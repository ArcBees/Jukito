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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Test that providers injected by the tester module behaves correctly.
 *
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class ProviderTest {

  /**
   * Guice test module.
   */
  static class Module extends JukitoModule {
    @Override
    protected void configureTest() {
      bindNamedMock(Mock.class, "singleton").in(TestScope.SINGLETON);
      bindNamedMock(Mock.class, "nonsingleton");
      bindNamed(Instance.class, "singleton").to(Instance.class).in(TestSingleton.class);
      bindNamed(Instance.class, "nonsingleton").to(Instance.class);
      bindNamed(Parent.class, "providerInstance").toProvider(new ParentProviderA()).in(
          TestSingleton.class);
      bindNamed(Parent.class, "providerClass").toProvider(ParentProviderB.class).in(
          TestSingleton.class);
      bindNamed(Parent.class, "providerKey").toProvider(Key.get(ParentProviderA.class)).in(
          TestSingleton.class);
      bindNamedMock(UninstanciableClass.class, "cannotInstantiate1").in(TestScope.SINGLETON);
      bind(UninstanciableClass.class).annotatedWith(Names.named("cannotInstantiate2")).toProvider(
          MyMockProvider2.class);
      bind(UninstanciableClass.class).annotatedWith(Names.named("cannotInstantiate3")).toProvider(
          Key.get(MyMockProvider3.class));
      bind(ClassWithMockedDependency1.class).annotatedWith(
          Names.named("MockedDependency1")).toProvider(MyProvider1.class);
      bind(ClassWithMockedDependency2.class).annotatedWith(
          Names.named("MockedDependency2")).toProvider(Key.get(MyProvider2.class));
    }
  }

  interface Mock { }

  static class Instance {
    @Inject Instance() { }
  }

  interface Parent {
    String getValue();
  }

  static class ChildA implements Parent {
    public String getValue() {
      return "childA";
    }
  }

  interface MockInChildB { }

  interface MockInProviderB {
    void test();
  }

  static class ChildB implements Parent {
    @Inject MockInChildB mockB;
    public String getValue() {
      return "childB";
    }
  }

  abstract static class ParentProviderABase implements Provider<Parent> {
  }

  static class ParentProviderA extends ParentProviderABase {
    @Override
    public Parent get() {
      return new ChildA();
    }
  }

  static class ParentProviderB implements Provider<Parent> {
    private final Provider<ChildB> childBProvider;

    @Inject
    ParentProviderB(Provider<ChildB> childBProvider, Provider<MockInProviderB> myMock) {
      this.childBProvider = childBProvider;

      // These calls should succeed
      myMock.get().test();
      verify(myMock.get()).test();
    }

    @Override
    public Parent get() {
      return childBProvider.get();
    }
  }

  static class UninstanciableClass {
    private UninstanciableClass() { }
    public int getValue() {
      return 42;
    }
  }

  static class MyMockProvider2 extends MockProvider<UninstanciableClass> {
    @Inject
    public MyMockProvider2() {
      super(UninstanciableClass.class);
    }
  }

  static class MyMockProvider3 extends MockProvider<UninstanciableClass> {
    @Inject
    public MyMockProvider3() {
      super(UninstanciableClass.class);
    }
  }

  interface DependencyShouldBeMocked1 {
    int getValue();
  }

  static class ClassWithMockedDependency1 {
    private final DependencyShouldBeMocked1 dependency;
    @Inject
    public ClassWithMockedDependency1(DependencyShouldBeMocked1 dependency) {
      this.dependency = dependency;
    }
    public DependencyShouldBeMocked1 getDependency() {
      return dependency;
    }
  }

  static class MyProvider1 implements Provider<ClassWithMockedDependency1> {
    final Provider<ClassWithMockedDependency1> provider;
    @Inject
    public MyProvider1(Provider<ClassWithMockedDependency1> provider) {
      this.provider = provider;
    }
    @Override
    public ClassWithMockedDependency1 get() {
      return provider.get();
    }
  }

  interface DependencyShouldBeMocked2 {
    int getValue();
  }

  static class ClassWithMockedDependency2 {
    private final DependencyShouldBeMocked2 dependency;
    @Inject
    public ClassWithMockedDependency2(DependencyShouldBeMocked2 dependency) {
      this.dependency = dependency;
    }
    public DependencyShouldBeMocked2 getDependency() {
      return dependency;
    }
  }

  static class MyProvider2 implements Provider<ClassWithMockedDependency2> {
    final Provider<ClassWithMockedDependency2> provider;
    @Inject
    public MyProvider2(Provider<ClassWithMockedDependency2> provider) {
      this.provider = provider;
    }
    @Override
    public ClassWithMockedDependency2 get() {
      return provider.get();
    }
  }

  @Test
  public void mockSingletonProviderShouldReturnTheSameInstance(
      @Named("singleton") Provider<Mock> provider) {
    assertSame(provider.get(), provider.get());
  }

  @Test
  public void mockNonSingletonProviderShouldNotReturnTheSameInstance(
      @Named("nonsingleton") Provider<Mock> provider) {
    assertNotSame(provider.get(), provider.get());
  }

  @Test
  public void singletonProvidedClassShouldReturnTheSameInstance(
      @Named("singleton") Provider<Instance> provider) {
    assertSame(provider.get(), provider.get());
  }

  @Test
  public void singletonClassShouldNotReturnTheSameInstance(
      @Named("singleton") Instance obj1, @Named("singleton") Instance obj2) {
    assertSame(obj1, obj2);
  }

  @Test
  public void nonSingletonProvidedClassShouldNotReturnTheSameInstance(
      @Named("nonsingleton") Provider<Instance> provider) {
    assertNotSame(provider.get(), provider.get());
  }

  @Test
  public void nonSingletonClassShouldNotReturnTheSameInstance(
      @Named("nonsingleton") Instance obj1, @Named("nonsingleton") Instance obj2) {
    assertNotSame(obj1, obj2);
  }

  @Test
  public void bindingToProviderInstanceShouldWorkAndInject(
      @Named("nonsingleton") Provider<Mock> provider) {
    assertNotSame(provider.get(), provider.get());
  }

  @Test
  public void shouldInjectProviderBoundWithInstance(
      @Named("providerInstance") Parent parentProvidedFromProviderInstance) {
    assertEquals(parentProvidedFromProviderInstance.getClass(), ChildA.class);
  }

  @Test
  public void shouldInjectProviderBoundWithClass(
      @Named("providerClass") Parent parentProvidedFromProviderClass) {
    assertEquals(parentProvidedFromProviderClass.getClass(), ChildB.class);
  }

  @Test
  public void shouldInjectProviderBoundWithKey(
      @Named("providerKey") Parent parentProvidedFromProviderKey) {
    assertEquals(parentProvidedFromProviderKey.getClass(), ChildA.class);
  }

  @Test
  public void shouldInjectProviderOfClassWithPrivateConstructor1(
      @Named("cannotInstantiate1") UninstanciableClass classWithPrivateConstructor) {
    verify(classWithPrivateConstructor, never()).getValue();
  }

  @Test
  public void shouldInjectProviderOfClassWithPrivateConstructor2(
      @Named("cannotInstantiate2") UninstanciableClass classWithPrivateConstructor) {
    verify(classWithPrivateConstructor, never()).getValue();
  }

  @Test
  public void shouldInjectProviderOfClassWithPrivateConstructor3(
      @Named("cannotInstantiate3") UninstanciableClass classWithPrivateConstructor) {
    verify(classWithPrivateConstructor, never()).getValue();
  }

  @Test
  public void testInjectingProviderShouldInstantiateDependencies1(
      @Named("MockedDependency1") ClassWithMockedDependency1 testClass) {
    verify(testClass.getDependency(), never()).getValue();
  }

  @Test
  public void testInjectingProviderShouldInstantiateDependencies2(
      @Named("MockedDependency2") ClassWithMockedDependency2 testClass) {
    verify(testClass.getDependency(), never()).getValue();
  }
}
