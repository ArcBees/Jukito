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

import com.google.inject.Provides;

import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Provider;

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
        public final Object getObject(Provider<Object> op) {
            return null;
        }

        @Override
        protected void configureTest() {
        }
    }

    @Test
    public void foo() {
        System.out.println("done");
    }
}
