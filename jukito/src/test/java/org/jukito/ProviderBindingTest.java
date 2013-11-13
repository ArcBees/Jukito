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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static junit.framework.Assert.assertNotNull;

/**
 * Test which ensures that a class which is a provider may also be binded independently.
 */
@RunWith(JukitoRunner.class)
public class ProviderBindingTest {

    /**
     * Guice test module.
     */
    public static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(ServiceAndProvider.class);
            bind(MyService.class).toProvider(ServiceAndProvider.class);
        }
    }

    @Test
    public void shouldBeExecuted(OtherService service, MyService myService) {
        assertNotNull(service);
        assertNotNull(myService);
    }

    interface MyService {
    }

    static class ServiceAndProvider implements Provider<MyService> {
        @Override
        public MyService get() {
            return Mockito.mock(MyService.class);
        }

        public void doSomethingVeryImportant() {
            // nop
        }
    }

    static class OtherService {
        @Inject
        public ServiceAndProvider service;
    }
}
