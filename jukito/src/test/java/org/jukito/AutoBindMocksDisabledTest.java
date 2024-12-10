/*
 * Copyright 2017 ArcBees Inc.
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.google.inject.PrivateModule;

import jakarta.inject.Inject;

/**
 * Tests behavior of autoBindMocks property on UseModules annotation.
 * NOTE: If autoBindMocks is true, this test will fail because SomeInterface will be auto bound
 * to a mock and the binding will conflict with private module's binding
 */
@RunWith(JukitoRunner.class)
@UseModules(value = AutoBindMocksDisabledTest.MyModule.class, autoBindMocks = false)
public class AutoBindMocksDisabledTest {

    /**
     * Guice PrivateModule for testing.
     */
    public static final class MyModule extends PrivateModule {

        @Override
        protected void configure() {
            bind(ExposedClass.class);
            bind(SomeInterface.class).to(NotAMock.class);
            expose(ExposedClass.class);
        }
    }

    /**
     * Test Interface that will be auto mocked if autoBindMocks is set to true.
     */
    public interface SomeInterface {
        void doSomething();
    }

    /**
     * When autoBindMocks is false, an instance of this type will be bound
     * to SomeInterface from the PrivateModule.
     */
    public static final class NotAMock implements SomeInterface {

        @Override
        public void doSomething() {
        }
    }

    /**
     * Class which injects either the automock or the concrete instance
     * depending on the autoBindMocks property.
     */
    public static final class ExposedClass {

        private SomeInterface instance;

        @Inject
        ExposedClass(final SomeInterface instance) {
            this.instance = instance;
        }

        SomeInterface getInstance() {
            return instance;
        }
    }

    @Test
    public void testSomething(final ExposedClass clazz) throws Exception {
        Assert.assertFalse(Mockito.mockingDetails(clazz).isMock());
        Assert.assertFalse(Mockito.mockingDetails(clazz.getInstance()).isMock());
    }
}
