/**
 * Copyright 2010 ArcBees Inc.
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jukito.BindingsCollector.BindingInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.internal.Errors;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;

/**
 * A guice {@link com.google.inject.Module Module} with a bit of syntactic sugar
 * to bind within typical test scopes. Depends on mockito. This module
 * automatically mocks any interface or abstract class dependency for which a
 * binding is not explicitly provided. Any concrete class for which a binding is
 * not explicitly provided is bound as a {@link TestScope#SINGLETON}.
 * <p />
 * Depends on Mockito.
 * 
 * @author Philippe Beaudoin
 */
public abstract class JukitoModule extends TestModule {

  protected List<BindingInfo> bindingsObserved = Collections.emptyList();
  private final Set<Class<?>> forceMock = new HashSet<Class<?>>();
  private final Set<Class<?>> dontForceMock = new HashSet<Class<?>>();
  private final List<Key<?>> keysNeedingTransitiveDependencies = new ArrayList<Key<?>>();

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
   * will be mocked in {@link org.jukito.TestMockSingleton} scope.
   * 
   * @param klass The {@link Class} or interface for which all subclasses will
   *          be mocked.
   */
  protected void forceMock(Class<?> klass) {
    forceMock.add(klass);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public final void configure() {
    bindScopes();
    configureTest();
    
    Set<Key<?>> keysObserved = new HashSet<Key<?>>(bindingsObserved.size());
    Set<Key<?>> keysNeeded = new HashSet<Key<?>>(bindingsObserved.size());

    for (BindingInfo bindingInfo : bindingsObserved) {
      if (bindingInfo.key != null) {
        keysObserved.add(bindingInfo.key);
      }
      if (bindingInfo.boundKey != null) {
        keysNeeded.add(bindingInfo.boundKey);
      }
      if (bindingInfo.boundInstance != null && bindingInfo.boundInstance instanceof HasDependencies) {
        HasDependencies hasDependencies = (HasDependencies) bindingInfo.boundInstance;
        for (Dependency<?> dependency : hasDependencies.getDependencies()) {
          keysNeeded.add(dependency.getKey());
        }
      }
    }

    // Make sure needed keys from Guice bindings are bound as mock or to instances (but not as test singletons)
    for (Key<?> keyNeeded : keysNeeded) {
      addNeededKey(keysObserved, keysNeeded, keyNeeded, false);
    }

    // Preempt JIT binding by looking through the test class and any base
    // class looking for nested classes annotated with @TestSingleton and
    // @TestEagerSingleton
    Class<?> currentClass = testClass;
    while (currentClass != null) {
      for (Class<?> subClass : testClass.getDeclaredClasses()) {
        Key<?> key = Key.get(subClass);
        if (!keysObserved.contains(key)) {
          if (subClass.isAnnotationPresent(TestSingleton.class)) {
            bind(subClass).in(TestScope.SINGLETON);
            keysObserved.add(key);
            keysNeeded.add(key);
            keysNeedingTransitiveDependencies.add(key);
          } else if (subClass.isAnnotationPresent(TestEagerSingleton.class)) {
            bind(subClass).in(TestScope.EAGER_SINGLETON);
            keysObserved.add(key);
            keysNeeded.add(key);
            keysNeedingTransitiveDependencies.add(key);
          } else if (subClass.isAnnotationPresent(TestMockSingleton.class)) {
            bindMock(subClass).in(TestScope.SINGLETON);
            keysObserved.add(key);
            keysNeeded.add(key);
          }
        }
      }
      currentClass = currentClass.getSuperclass();
    }

    // Preempt JIT binding by looking through the test class looking for
    // methods annotated with @Test, @Before, or @After
    // Concrete classes bound in this way are singleton
    currentClass = testClass;
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
    // fields and methods annotated with @Inject
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

    // Recursively add the dependencies of all the bindings observed
    for (int i = 0; i < keysNeedingTransitiveDependencies.size(); ++i) {
      addDependencies(keysNeedingTransitiveDependencies.get(i), keysObserved, keysNeeded);
    }

    // Bind all keys needed but not observed as mocks
    for (Key<?> key : keysNeeded) {
      Class<?> rawType = key.getTypeLiteral().getRawType();
      if (!keysObserved.contains(key) && !isCoreGuiceType(rawType)
          && !isAssistedInjection(key)) {
        super.bind(key).toProvider(new MockProvider(rawType)).in(
            TestScope.SINGLETON);
      }
    }
  }

  private void addNeededKey(Set<Key<?>> keysObserved, Set<Key<?>> keysNeeded,
      Key<?> keyNeeded, boolean asTestSingleton) {
    keysNeeded.add(keyNeeded);
    bindIfConcrete(keysObserved, keyNeeded, asTestSingleton);
  }

  private <T> void bindIfConcrete(Set<Key<?>> keysObserved,
      Key<T> key, boolean asTestSingleton) {
    TypeLiteral<?> parameter = key.getTypeLiteral();
    Class<?> rawType = parameter.getRawType();
    if (isInstantiable(rawType) && !shouldForceMock(rawType)
        && !isCoreGuiceType(rawType) && !isAssistedInjection(key)
        && !keysObserved.contains(key)) {
      if (asTestSingleton) {
        bind(key).in(TestScope.SINGLETON);
      } else {
        bind(key);
      }
      keysObserved.add(key);
      keysNeedingTransitiveDependencies.add(key);
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
    if (!isInstantiable(type.getRawType())) {
      return;
    }
    addInjectionPointDependencies(InjectionPoint.forConstructorOf(type),
        keysObserved, keysNeeded);
    Set<InjectionPoint> methodsAndFieldsInjectionPoints = InjectionPoint.forInstanceMethodsAndFields(type);
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
    if (Provider.class.isAssignableFrom(key.getTypeLiteral().getRawType())) {
      Type providedType = ((ParameterizedType) key.getTypeLiteral().getType()).getActualTypeArguments()[0];
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
}
