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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test exercising Guice's install() mechanism.
 */
@RunWith(JukitoRunner.class)
public class InstallTest {

    /**
     * Guice test module, containing two submodules.
     */
    static class Module extends JukitoModule {

        public class FooModule extends AbstractModule {

            @Override
            protected void configure() {
                bind(Foo.class).to(FooImpl.class);
            }
        }

        public class BarModule extends AbstractModule {

            @Override
            protected void configure() {
                bind(Bar.class).to(BarImpl.class);
            }
        }

        @Override
        protected void configureTest() {
            install(new FooModule());
            install(new BarModule());
            bindNamedMock(Foo.class, "ten").in(TestSingleton.class);
        }
    }

    interface Foo {
        int calc();
    }

    static class FooImpl implements Foo {

        private final Provider<Bar> barProvider;
        private final Foo ten;

        @Inject
        public FooImpl(Provider<Bar> barProvider, @Named("10") Foo ten) {
            this.barProvider = barProvider;
            this.ten = ten;
        }

        @Override
        public int calc() {
            return this.barProvider.get().calc() + ten.calc();
        }
    }

    interface Bar {
        int calc();
    }

    static class BarImpl implements Bar {
        @Override
        public int calc() {
            return 5;
        }
    }

    @Inject
    Foo foo;

    @Before
    public void setup(@Named("10") Foo ten) {
        when(ten.calc()).thenReturn(10);
    }

    @Test
    public void installingModuleWorks(Bar bar) {
        assertEquals(15, foo.calc());
        assertEquals(5, bar.calc());
    }
}
