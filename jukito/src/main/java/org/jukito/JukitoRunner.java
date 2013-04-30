/**
 * Copyright 2011 ArcBees Inc.
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

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scope;
import com.google.inject.internal.Errors;
import com.google.inject.spi.DefaultBindingScopingVisitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.mockito.internal.runners.util.FrameworkUsageValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Rework this documentation
 * <p/>
 * This class implements the mockito runner but allows Guice dependency
 * injection. To setup the guice environment, the test class can have an inner
 * static class deriving from {@link TestModule}. This last class will let you bind
 * {@link TestSingleton} and {@link TestEagerSingleton} and the runner will make sure these
 * singletons are reset at every invocation of a test case.
 * <p/>
 * This code not very clean as it is cut & paste from
 * {@link org.mockito.internal.runners.JUnit45AndHigherRunnerImpl}, but it's
 * unclear how we could make otherwise.
 * <p/>
 * Most of the code here is inspired from: <a href=
 * "http://cowwoc.blogspot.com/2008/10/integrating-google-guice-into-junit4.html"
 * > http://cowwoc.blogspot.com/2008/10/integrating-google-guice-into-junit4.
 * html</a>
 * <p/>
 * Depends on Mockito.
 *
 * @author Philippe Beaudoin
 */
public class JukitoRunner extends BlockJUnit4ClassRunner {

    private static final boolean useAutomockingIfNoEnvironmentFound = true;
    private Injector injector;

    public JukitoRunner(Class<?> klass) throws InitializationError,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        super(klass);
        ensureInjector();
    }

    public JukitoRunner(Class<?> klass, Injector injector) throws InitializationError,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        // refactor needed here cos ensureInjector is run without reason here.
        super(klass);
        this.injector = injector;
    }

    private void ensureInjector()
            throws InstantiationException, IllegalAccessException {
        if (injector != null) {
            return;
        }
        Class<?> testClass = getTestClass().getJavaClass();
        TestModule testModule = getTestModule(testClass);
        testModule.setTestClass(testClass);

        JukitoModule jukitoModule = null; // Only non-null if it's a JukitoModule
        if (testModule instanceof JukitoModule) {
            jukitoModule = (JukitoModule) testModule;

            // Create a module just for the purpose of collecting bindings
            TestModule testModuleForCollection = getTestModule(testClass);
            BindingsCollector collector = new BindingsCollector(testModuleForCollection);
            collector.collectBindings();
            jukitoModule.setBindingsObserved(collector.getBindingsObserved());
        }
        injector = Guice.createInjector(testModule);
        if (jukitoModule != null && jukitoModule.getReportWriter() != null) {
            // An output report is desired
            BindingsCollector collector = new BindingsCollector(jukitoModule);
            collector.collectBindings();
            jukitoModule.printReport(collector.getBindingsObserved());
        }
    }

    private TestModule getTestModule(Class<?> testClass) throws InstantiationException, IllegalAccessException {
        UseModules useModules = testClass.getAnnotation(UseModules.class);
        if (useModules != null) {
            Class<? extends Module>[] moduleClasses = useModules.value();
            return createJukitoModule(moduleClasses);
        }

        TestModule testModule = null;
        for (Class<?> innerClass : testClass.getDeclaredClasses()) {
            if (TestModule.class.isAssignableFrom(innerClass)) {
                assert testModule == null :
                        "More than one TestModule inner class found within test class \""
                                + testClass.getName() + "\".";
                testModule = (TestModule) innerClass.newInstance();
            }
        }
        if (testModule != null) {
            return testModule;
        }

        if (useAutomockingIfNoEnvironmentFound) {
            return new JukitoModule() {
                @Override
                protected void configureTest() {
                }
            };
        } else {
            return new TestModule() {
                @Override
                protected void configureTest() {
                }
            };
        }
    }

    private JukitoModule createJukitoModule(Class<? extends Module>[] moduleClasses)
            throws InstantiationException, IllegalAccessException {
        final Module[] modules = new Module[moduleClasses.length];
        for (int i = 0; i < modules.length; i++) {
            modules[i] = moduleClasses[i].newInstance();
        }
        return new JukitoModule() {
            @Override
            protected void configureTest() {
                for (Module m : modules) {
                    install(m);
                }
            }
        };
    }

    @Override
    public void run(final RunNotifier notifier) {
        // add listener that validates framework usage at the end of each test
        notifier.addListener(new FrameworkUsageValidator(notifier));
        super.run(notifier);
    }

    @Override
    protected Object createTest() throws Exception {
        TestScope.clear();
        instantiateEagerTestSingletons();
        return injector.getInstance(getTestClass().getJavaClass());
    }

    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
        return new InjectedStatement(method, test, injector);
    }

    @Override
    protected Statement withBefores(FrameworkMethod method, Object target,
                                    Statement statement) {
        try {
            ensureInjector();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(
                Before.class);
        return befores.isEmpty() ? statement : new InjectedBeforeStatements(statement,
                befores, target, injector);
    }

    @Override
    protected Statement withAfters(FrameworkMethod method, Object target,
                                   Statement statement) {
        try {
            ensureInjector();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(
                After.class);
        return afters.isEmpty() ? statement : new InjectedAfterStatements(statement,
                afters, target, injector);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        try {
            ensureInjector();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<FrameworkMethod> testMethods = getTestClass().getAnnotatedMethods(Test.class);
        List<FrameworkMethod> result = new ArrayList<FrameworkMethod>(testMethods.size());
        for (FrameworkMethod method : testMethods) {
            Method javaMethod = method.getMethod();
            Errors errors = new Errors(javaMethod);
            List<Key<?>> keys = GuiceUtils.getMethodKeys(javaMethod, errors);
            errors.throwConfigurationExceptionIfErrorsExist();

            List<List<Binding<?>>> bindingsToUseForParameters = new ArrayList<List<Binding<?>>>();
            for (Key<?> key : keys) {
                if (All.class.equals(key.getAnnotationType())) {
                    List<Binding<?>> bindings = new ArrayList<Binding<?>>();
                    for (Binding<?> binding : injector.findBindingsByType(key.getTypeLiteral())) {
                        bindings.add(binding);
                    }
                    bindingsToUseForParameters.add(bindings);
                }
            }

            // Add an injected method for every combination of binding
            addAllBindingAssignations(bindingsToUseForParameters, 0,
                    new ArrayList<Binding<?>>(bindingsToUseForParameters.size()),
                    javaMethod, result);
        }

        return result;
    }

    /**
     * This method looks at all possible way to assign the bindings in
     * {@code bindingsToUseForParameters}, starting at index {@code index}.
     * If {@code index} is larger than the number of elements in {@code bindingsToUseForParameters}
     * then the {@code currentAssignation} with {@javaMethod} is added to {@code result}.
     *
     * @param result
     * @param javaMethod
     * @param bindingsToUseForParameters
     * @param index
     * @param currentAssignation
     */
    private void addAllBindingAssignations(
            List<List<Binding<?>>> bindingsToUseForParameters, int index,
            List<Binding<?>> currentAssignation,
            Method javaMethod, List<FrameworkMethod> result) {

        if (index >= bindingsToUseForParameters.size()) {
            List<Binding<?>> assignation = new ArrayList<Binding<?>>(currentAssignation.size());
            assignation.addAll(currentAssignation);
            result.add(new InjectedFrameworkMethod(javaMethod, assignation));
            return;
        }

        for (Binding<?> binding : bindingsToUseForParameters.get(index)) {
            currentAssignation.add(binding);
            if (currentAssignation.size() != index + 1) {
                throw new AssertionError("Size of currentAssignation list is wrong.");
            }
            addAllBindingAssignations(bindingsToUseForParameters, index + 1,
                    currentAssignation,
                    javaMethod, result);
            currentAssignation.remove(index);
        }
    }

    private void instantiateEagerTestSingletons() {
        DefaultBindingScopingVisitor<Boolean> isEagerTestScopeSingleton =
                new DefaultBindingScopingVisitor<Boolean>() {
                    public Boolean visitScope(Scope scope) {
                        return scope == TestScope.EAGER_SINGLETON;
                    }
                };
        for (Binding<?> binding : injector.getBindings().values()) {
            boolean instantiate = false;
            if (binding != null) {
                Boolean result = binding.acceptScopingVisitor(isEagerTestScopeSingleton);
                if (result != null && result) {
                    instantiate = true;
                }
            }
            if (instantiate) {
                binding.getProvider().get();
            }
        }
    }

    /**
     * Adds to {@code errors} for each method annotated with {@code @Test},
     * {@code @Before}, or {@code @After} that is not a public, void instance
     * method with no arguments.
     */
    protected void validateInstanceMethods(List<Throwable> errors) {
        validatePublicVoidMethods(After.class, false, errors);
        validatePublicVoidMethods(Before.class, false, errors);
        validateTestMethods(errors);

        if (computeTestMethods().size() == 0) {
            errors.add(new Exception("No runnable methods"));
        }
    }

    /**
     * Adds to {@code errors} for each method annotated with {@code @Test}that
     * is not a public, void instance method with no arguments.
     */
    protected void validateTestMethods(List<Throwable> errors) {
        validatePublicVoidMethods(Test.class, false, errors);
    }

    /**
     * Adds to {@code errors} if any method in this class is annotated with
     * the provided {@code annotation}, but:
     * <ul>
     * <li>is not public, or
     * <li>returns something other than void, or
     * <li>is static (given {@code isStatic is false}), or
     * <li>is not static (given {@code isStatic is true}).
     */
    protected void validatePublicVoidMethods(Class<? extends Annotation> annotation,
                                             boolean isStatic, List<Throwable> errors) {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);

        for (FrameworkMethod eachTestMethod : methods) {
            eachTestMethod.validatePublicVoid(isStatic, errors);
        }
    }

    /**
     * Access the Guice injector.
     *
     * @return The Guice {@link Injector}.
     */
    protected Injector getInjector() {
        return injector;
    }
}
