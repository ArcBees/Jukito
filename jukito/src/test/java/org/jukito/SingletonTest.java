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

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Test that the various flavours of singletons work correctly.
 *
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class SingletonTest {

    /**
     * Guice test module.
     */
    static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(MyEagerSingleton.class).asEagerSingleton();
            bindMock(MyTestMockSingletonBoundNonMock.class);
            bind(MyTestEagerSingleton.class);
        }
    }

    @TestSingleton
    static class Registry {
        public Map<Class<?>, Integer> registrationCount = new HashMap<Class<?>, Integer>();

        public void register(Class<?> clazz) {
            registrationCount.put(clazz, getCount(clazz) + 1);
        }

        public int getCount(Class<?> clazz) {
            Integer value = registrationCount.get(clazz);
            if (value == null) {
                return 0;
            }
            return value;
        }
    }

    /**
     * This class keeps track of what happens in all the tests run in this
     * class. It's used to make sure all expected tests are called.
     */
    private static class Bookkeeper {
        static int numberOfTimesTestEagerSingletonIsInstantiated;
        static int numberOfTimesTestSingletonIsInstantiated;
        static int numberOfTimesEagerSingletonIsInstantiated;
        static ExternalSingleton singleton1;
        static ExternalSingleton singleton2;
    }

    /**
     * This should be instantiated once for the entire test class.
     */
    static class MyEagerSingleton {
        @Inject
        public MyEagerSingleton(Registry registry) {
            registry.register(getClass());
            Bookkeeper.numberOfTimesEagerSingletonIsInstantiated++;
        }
    }

    /**
     * This should automatically register before each test.
     */
    @TestEagerSingleton
    static class MyTestEagerSingleton {
        @Inject
        public MyTestEagerSingleton(Registry registry) {
            registry.register(getClass());
            Bookkeeper.numberOfTimesTestEagerSingletonIsInstantiated++;
        }
    }

    /**
     * This should register only in tests where it is injected.
     */
    @TestSingleton
    static class MyTestSingleton {
        @Inject
        public MyTestSingleton(Registry registry) {
            registry.register(getClass());
            Bookkeeper.numberOfTimesTestSingletonIsInstantiated++;
        }
    }

    /**
     * This should be different from one test to the next.
     */
    @TestMockSingleton
    interface MyTestMockSingleton {
        void dummy();
    }

    /**
     * This should be bound as non-mock even though there is an annotation,
     * because the module explicitely binds it.
     */
    @TestMockSingleton
    interface MyTestMockSingletonBoundNonMock {
        void dummy();
    }

    @Inject
    Registry registry;

    @Test
    public void onlyEagerSingletonShouldBeRegistered() {
        assertEquals(1, registry.getCount(MyTestEagerSingleton.class));
    }

    @Test
    public void bothSingletonsShouldBeRegistered(MyTestSingleton myTestSingleton) {
        assertEquals(1, registry.getCount(MyTestEagerSingleton.class));
        assertEquals(1, registry.getCount(MyTestSingleton.class));
    }

    @Test
    public void injectionOfMockShouldBeADifferentObject1(MyTestMockSingleton myTestMockSingleton) {
        myTestMockSingleton.dummy();
        verify(myTestMockSingleton).dummy();
    }

    @Test
    public void injectionOfMockShouldBeADifferentObject2(MyTestMockSingleton myTestMockSingleton) {
        myTestMockSingleton.dummy();
        verify(myTestMockSingleton).dummy();
    }

    @Test
    public void injectionOfSingletonMockExplicitelyBoundAsNonSingleton(
            MyTestMockSingletonBoundNonMock a,
            MyTestMockSingletonBoundNonMock b) {
        verify(a, never()).dummy();
        verify(b, never()).dummy();
        assertNotSame(a, b);
    }

    @Test
    public void firstInjectionOfSingleton(ExternalSingleton obj) {
        Bookkeeper.singleton1 = obj;
    }

    @Test
    public void secondInjectionOfSingleton(ExternalSingleton obj) {
        Bookkeeper.singleton2 = obj;
    }

    @AfterClass
    public static void verifyNumberOfInstantiations() {
        assertEquals(7, Bookkeeper.numberOfTimesTestEagerSingletonIsInstantiated);
        assertEquals(1, Bookkeeper.numberOfTimesTestSingletonIsInstantiated);
        assertEquals(1, Bookkeeper.numberOfTimesEagerSingletonIsInstantiated);
        assertNotSame(Bookkeeper.singleton1, Bookkeeper.singleton2);
    }

}
