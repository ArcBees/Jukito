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

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Provides;

import jakarta.inject.Provider;

import static org.junit.Assert.assertEquals;

/**
 * A test to make sure injecting a Provider in a @Provides method.
 * See https://github.com/ArcBees/Jukito/issues/34
 */
@RunWith(JukitoRunner.class)
public class BindingToProviderTest {

    /**
     * Guice test module.
     */
    public static class MyModule extends JukitoModule {
        @Provides
        public final MyClass getObject(Provider<String> provider) {
            return new MyClass("abc" + provider.get());
        }

        @Override
        protected void configureTest() {
            bind(String.class).toInstance("def");
        }
    }

    static class MyClass {
        private final String string;

        MyClass(String string) {
            this.string = string;
        }

        String getString() {
            return string;
        }
    }

    @Test
    public void foo(MyClass obj) {
        assertEquals("abcdef", obj.getString());
    }
}
