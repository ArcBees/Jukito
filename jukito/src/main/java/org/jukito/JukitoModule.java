/**
 * Copyright 2013 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.jukito;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ProviderMethod;
import com.google.inject.internal.ProviderMethodsModule;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;

import org.jukito.BindingsCollector.BindingInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A guice {@link com.google.inject.Module Module} with a bit of syntactic sugar
 * to bind within typical test scopes. Depends on mockito. This module
 * automatically mocks any interface or abstract class dependency for which a
 * binding is not explicitly provided. Any concrete class for which a binding is
 * not explicitly provided is bound as a {@link TestScope#SINGLETON}.
 * <p/>
 * Depends on Mockito.
 */
public abstract class JukitoModule extends TestModule {

    protected List<BindingInfo> bindingsObserved = Collections.emptyList();

    private final Set<Class<?>> forceMock = new HashSet<>();
    private final Set<Class<?>> dontForceMock = new HashSet<>();
    private final List<Key<?>> keysNeedingTransitiveDependencies = new ArrayList<>();
    private final Map<Class<?>, Object> primitiveTypes = new HashMap<>();

    public JukitoModule() {
        primitiveTypes.put(String.class, "");
        primitiveTypes.put(Integer.class, 0);
        primitiveTypes.put(Long.class, 0L);
        primitiveTypes.put(Boolean.class, false);
        primitiveTypes.put(Double.class, 0.0);
        primitiveTypes.put(Float.class, 0.0f);
        primitiveTypes.put(Short.class, (short) 0);
        primitiveTypes.put(Character.class, '\0');
        primitiveTypes.put(Byte.class, (byte) 0);
        primitiveTypes.put(Class.class, Object.class);
    }

    /**
     * Attach this {@link JukitoModule} to a list of the bindings that were
     * observed by a preliminary run of {@link BindingsCollector}.
     *
     * @param bindingsObserved The observed bindings.
     */
    public void setBindingsObserved(List<BindingInfo> bindingsObserved) {
        this.bindingsObserved = bindingsObserved;
    }

    /**
     * By default, only abstract classes, interfaces and classes annotated with
     * {@link TestMockSingleton} are automatically mocked. Use {@link #forceMock}
     * to indicate that all concrete classes derived from the a specific type
     * will be mocked in {@link TestMockSingleton} scope.
     *
     * @param klass The {@link Class} or interface for which all subclasses will
     *              be mocked.
     */
    protected void forceMock(Class<?> klass) {
        forceMock.add(klass);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void configure() {
        bindScopes();
        configureTest();

        Set<Key<?>> keysObserved = new HashSet<>(bindingsObserved.size());
        Set<Key<?>> keysNeeded = new HashSet<>(bindingsObserved.size());

        for (BindingInfo bindingInfo : bindingsObserved) {
            if (bindingInfo.key != null) {
                keysObserved.add(bindingInfo.key);
            }
            if (bindingInfo.boundKey != null) {
                keysNeeded.add(bindingInfo.boundKey);
            }
            if (bindingInfo.boundInstance != null &&
                    bindingInfo.boundInstance instanceof HasDependencies) {
                HasDependencies hasDependencies = (HasDependencies) bindingInfo.boundInstance;
                for (Dependency<?> dependency : hasDependencies.getDependencies()) {
                    keysNeeded.add(dependency.getKey());
                }
            }
        }

        // registering keys build via @Provides methods in this module in the keysObserved set.
        ProviderMethodsModule providerMethodsModule = (ProviderMethodsModule)
                ProviderMethodsModule.forModule(this);

        List<ProviderMethod<?>> providerMethodList = providerMethodsModule.getProviderMethods(binder());
        for (ProviderMethod<?> providerMethod : providerMethodList) {
            keysObserved.add(providerMethod.getKey());
        }

        // Make sure needed keys from Guice bindings are bound as mock or to instances
        // (but not as test singletons)
        for (Key<?> keyNeeded : keysNeeded) {
            addNeededKey(keysObserved, keysNeeded, keyNeeded, false);
            keysNeedingTransitiveDependencies.add(keyNeeded);
        }

        // Preempt JIT binding by looking through the test class and any parent class
        // looking for methods annotated with @Test, @Before, or @After.
        // Concrete classes bound in this way are bound in @TestSingleton.
        Class<?> currentClass = testClass;
        while (currentClass != null) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)
                        || method.isAnnotationPresent(Before.class)
                        || method.isAnnotationPresent(After.class)) {

                    Errors errors = new Errors(method);
                    List<Key<?>> keys = GuiceUtils.getMethodKeys(method, errors);

                    for (Key<?> key : keys) {
                        // Skip keys annotated with @All
                        if (!All.class.equals(key.getAnnotationType())) {
                            Key<?> keyNeeded = GuiceUtils.ensureProvidedKey(key, errors);
                            addNeededKey(keysObserved, keysNeeded, keyNeeded, true);
                        }
                    }
                    errors.throwConfigurationExceptionIfErrorsExist();
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        // Preempt JIT binding by looking through the test class looking for
        // fields and methods annotated with @Inject.
        // Concrete classes bound in this way are bound in @TestSingleton.
        if (testClass != null) {
            Set<InjectionPoint> injectionPoints = InjectionPoint.forInstanceMethodsAndFields(testClass);

            for (InjectionPoint injectionPoint : injectionPoints) {
                Errors errors = new Errors(injectionPoint);
                List<Dependency<?>> dependencies = injectionPoint.getDependencies();
                for (Dependency<?> dependency : dependencies) {
                    Key<?> keyNeeded = GuiceUtils.ensureProvidedKey(dependency.getKey(),
                            errors);
                    addNeededKey(keysObserved, keysNeeded, keyNeeded, true);
                }
                errors.throwConfigurationExceptionIfErrorsExist();
            }
        }

        // Recursively add the dependencies of all the bindings observed. Warning, we can't use for each here
        // since it would result into concurrency issues.
        for (int i = 0; i < keysNeedingTransitiveDependencies.size(); ++i) {
            addDependencies(keysNeedingTransitiveDependencies.get(i), keysObserved, keysNeeded);
        }

        // Bind all keys needed but not observed as mocks.
        for (Key<?> key : keysNeeded) {
            Class<?> rawType = key.getTypeLiteral().getRawType();
            if (!keysObserved.contains(key) && !isCoreGuiceType(rawType)
                    && !isAssistedInjection(key)) {
                Object primitiveInstance = getDummyInstanceOfPrimitiveType(rawType);
                if (primitiveInstance == null) {
                    if (rawType != Provider.class) {
                        bind(key).toProvider(new MockProvider(rawType)).in(TestScope.SINGLETON);
                    }
                } else {
                    bindKeyToInstance(key, primitiveInstance);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void bindKeyToInstance(Key<T> key, Object primitiveInstance) {
        bind(key).toInstance((T) primitiveInstance);
    }

    private void addNeededKey(Set<Key<?>> keysObserved, Set<Key<?>> keysNeeded,
            Key<?> keyNeeded, boolean asTestSingleton) {
        keysNeeded.add(keyNeeded);
        bindIfConcrete(keysObserved, keyNeeded, asTestSingleton);
    }

    private <T> void bindIfConcrete(Set<Key<?>> keysObserved,
            Key<T> key, boolean asTestSingleton) {
        TypeLiteral<?> typeToBind = key.getTypeLiteral();
        Class<?> rawType = typeToBind.getRawType();
        if (!keysObserved.contains(key) && canBeInjected(typeToBind)
                && !shouldForceMock(rawType) && !isAssistedInjection(key)) {

            // If an @Singleton annotation is present, force the bind as TestSingleton
            if (asTestSingleton ||
                    rawType.getAnnotation(Singleton.class) != null) {
                bind(key).in(TestScope.SINGLETON);
            } else {
                bind(key);
            }
            keysObserved.add(key);
            keysNeedingTransitiveDependencies.add(key);
        }
    }

    private boolean canBeInjected(TypeLiteral<?> type) {
        Class<?> rawType = type.getRawType();
        if (isPrimitive(rawType) || isCoreGuiceType(rawType) || !isInstantiable(rawType)) {
            return false;
        }
        try {
            InjectionPoint.forConstructorOf(type);
            return true;
        } catch (ConfigurationException e) {
            return false;
        }
    }

    private boolean isAssistedInjection(Key<?> key) {
        return key.getAnnotationType() != null
                && Assisted.class.isAssignableFrom(key.getAnnotationType());
    }

    private boolean shouldForceMock(Class<?> klass) {
        if (dontForceMock.contains(klass)) {
            return false;
        }
        if (forceMock.contains(klass)) {
            return true;
        }
        // The forceMock set contains all the base classes the user wants
        // to force mock, check id the specified klass is a subclass of one of
        // these.
        // Update forceMock or dontForceMock based on the result to speed-up
        // future look-ups.
        boolean result = false;
        for (Class<?> classToMock : forceMock) {
            if (classToMock.isAssignableFrom(klass)) {
                result = true;
                break;
            }
        }

        if (result) {
            forceMock.add(klass);
        } else {
            dontForceMock.add(klass);
        }

        return result;
    }

    private boolean isInstantiable(Class<?> klass) {
        return !klass.isInterface() && !Modifier.isAbstract(klass.getModifiers());
    }

    private boolean isPrimitive(Class<?> klass) {
        return getDummyInstanceOfPrimitiveType(klass) != null;
    }

    private Object getDummyInstanceOfPrimitiveType(Class<?> klass) {
        Object instance = primitiveTypes.get(klass);
        if (instance == null && Enum.class.isAssignableFrom(klass)) {
            // Safe to ignore exception, Guice will fail with a reasonable error
            // if the Enum is empty.
            try {
                instance = ((Object[]) klass.getMethod("values").invoke(null))[0];
            } catch (Exception ignored) {
            }
        }
        return instance;
    }

    private boolean isCoreGuiceType(Class<?> klass) {
        return TypeLiteral.class.isAssignableFrom(klass)
                || Injector.class.isAssignableFrom(klass)
                || Logger.class.isAssignableFrom(klass)
                || Stage.class.isAssignableFrom(klass)
                || MembersInjector.class.isAssignableFrom(klass);
    }

    private <T> void addDependencies(Key<T> key, Set<Key<?>> keysObserved,
            Set<Key<?>> keysNeeded) {
        TypeLiteral<T> type = key.getTypeLiteral();
        if (!canBeInjected(type)) {
            return;
        }
        addInjectionPointDependencies(InjectionPoint.forConstructorOf(type),
                keysObserved, keysNeeded);
        Set<InjectionPoint> methodsAndFieldsInjectionPoints =
                InjectionPoint.forInstanceMethodsAndFields(type);
        for (InjectionPoint injectionPoint : methodsAndFieldsInjectionPoints) {
            addInjectionPointDependencies(injectionPoint, keysObserved, keysNeeded);
        }
    }

    private void addInjectionPointDependencies(InjectionPoint injectionPoint,
            Set<Key<?>> keysObserved, Set<Key<?>> keysNeeded) {
        // Do not consider dependencies coming from optional injections
        if (injectionPoint.isOptional()) {
            return;
        }
        for (Dependency<?> dependency : injectionPoint.getDependencies()) {
            Key<?> key = dependency.getKey();
            addKeyDependency(key, keysObserved, keysNeeded);
        }
    }

    private void addKeyDependency(Key<?> key, Set<Key<?>> keysObserved,
            Set<Key<?>> keysNeeded) {
        Key<?> newKey = key;
        if (Provider.class.equals(key.getTypeLiteral().getRawType())) {
            Type providedType = (
                    (ParameterizedType) key.getTypeLiteral().getType()).getActualTypeArguments()[0];
            if (key.getAnnotation() != null) {
                newKey = Key.get(providedType, key.getAnnotation());
            } else if (key.getAnnotationType() != null) {
                newKey = Key.get(providedType, key.getAnnotationType());
            } else {
                newKey = Key.get(providedType);
            }
        }
        addNeededKey(keysObserved, keysNeeded, newKey, true);
    }

    /**
     * Override and return the {@link Writer} you want to use to report the tree of test objects,and whether they
     * were mocked, spied, automatically instantiated, or explicitly bound. Mostly useful for
     * debugging.
     *
     * @return The {@link Writer}, if {@code null} no report will be output.
     */
    public Writer getReportWriter() {
        return null;
    }

    /**
     * Outputs the report, see {@link #setReportWriter(Writer)}. Will not output anything if the
     * {@code reportWriter} is {@code null}. Do not call directly, it will be called by
     * {@link JukitoRunner}. To obtain a report, override {@link #getReportWriter()}.
     */
    public void printReport(List<BindingInfo> allBindings) {
        Writer reportWriter = getReportWriter();
        if (reportWriter == null) {
            return;
        }

        try {
            reportWriter.append("*** EXPLICIT BINDINGS ***\n");
            Set<Key<?>> reportedKeys = outputBindings(reportWriter, bindingsObserved,
                    Collections.<Key<?>>emptySet());
            reportWriter.append('\n');
            reportWriter.append("*** AUTOMATIC BINDINGS ***\n");
            outputBindings(reportWriter, allBindings, reportedKeys);
            reportWriter.append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param reportWriter The {@link Writer} to use to output the report.
     * @param bindings     The bindings to report.
     * @param keysToSkip   The keys that should not be reported.
     * @return All the keys that were reported.
     * @throws IOException If something goes wrong when writing.
     */
    private Set<Key<?>> outputBindings(Writer reportWriter, List<BindingInfo> bindings,
            Set<Key<?>> keysToSkip) throws IOException {

        Set<Key<?>> reportedKeys = new HashSet<>(bindings.size());
        for (BindingInfo bindingInfo : bindings) {
            if (keysToSkip.contains(bindingInfo.key)) {
                continue;
            }
            reportedKeys.add(bindingInfo.key);
            reportWriter.append("  ");
            reportWriter.append(bindingInfo.key.toString());
            reportWriter.append(" --> ");
            if (bindingInfo.boundKey != null) {
                if (bindingInfo.key == bindingInfo.boundKey) {
                    reportWriter.append("Bound directly");
                } else {
                    reportWriter.append(bindingInfo.boundKey.toString());
                }
            } else if (bindingInfo.boundInstance != null) {
                reportWriter.append("Instance of ").append(bindingInfo.boundInstance.getClass().getCanonicalName());
            } else {
                reportWriter.append("NOTHING!?");
            }
            reportWriter.append(" ### ");
            if (bindingInfo.scope == null) {
                reportWriter.append("No scope");
            } else {
                reportWriter.append("In scope ");
                reportWriter.append(bindingInfo.scope);
            }
            reportWriter.append('\n');
        }
        return reportedKeys;
    }
}
