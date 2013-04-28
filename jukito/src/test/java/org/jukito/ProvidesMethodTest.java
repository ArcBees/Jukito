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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Test that @Provides methods in the tester module behave correctly.
 *
 * @author Trask Stalnaker
 * @author tpeierls@gmail.com
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class ProvidesMethodTest {

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
            bindNamedMock(UninstanciableClass.class, "cannotInstantiate1").in(TestScope.SINGLETON);
        }

        @Provides
        @TestSingleton
        @Named("providerInstance")
        protected Parent providesParent1() {
            return new ChildA();
        }

        @Provides
        @TestSingleton
        @Named("providerClass")
        protected Parent providesParent2(ChildB childB, MockInProviderB myMock) {
            // These calls should succeed
            myMock.test();
            verify(myMock).test();
            return childB;
        }

        @Provides
        @TestSingleton
        @Named("providerKey")
        protected Parent providesParent3() {
            return new ChildA();
        }

        @Provides
        @TestSingleton
        @Named("cannotInstantiate2")
        protected UninstanciableClass providesUninstanciableClass2() {
            return mock(UninstanciableClass.class);
        }

        @Provides
        @TestSingleton
        @Named("cannotInstantiate3")
        protected UninstanciableClass providesUninstanciableClass3() {
            return mock(UninstanciableClass.class);
        }

        @Provides
        @TestSingleton
        @Named("MockedDependency1")
        protected ClassWithMockedDependency1 providesClassWithMockedDependency1(ClassWithMockedDependency1 x) {
            return x;
        }

        @Provides
        @TestSingleton
        @Named("MockedDependency2")
        protected ClassWithMockedDependency2 providesClassWithMockedDependency2(ClassWithMockedDependency2 x) {
            return x;
        }

        @Provides
        public Value aValue() {
            return VALUE;
        }

        @Provides
        public Integer anInteger(Value value) {
            return 3;
        }
    }

    interface Mock {
    }

    static class Instance {
        @Inject
        Instance() {
        }
    }

    interface Parent {
        String getValue();
    }

    static class ChildA implements Parent {
        public String getValue() {
            return "childA";
        }
    }

    interface MockInChildB {
    }

    interface MockInProviderB {
        void test();
    }

    static class ChildB implements Parent {
        @Inject
        MockInChildB mockB;

        public String getValue() {
            return "childB";
        }
    }

    static class UninstanciableClass {
        private UninstanciableClass() {
        }

        public int getValue() {
            return 42;
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

    static class Value {
        public final String string;

        public Value(String string) {
            this.string = string;
        }
    }

    private static final Value VALUE = new Value("ok");

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

    @Test
    public void testProvidingConstants(Value value, Integer integer) {
        assertEquals("ok", value.string);
        assertEquals(3, (int) integer);
    }
}
