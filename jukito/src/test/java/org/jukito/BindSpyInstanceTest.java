/**
 * Copyright 2014 ArcBees Inc.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Bind spy instance test.
 */
@RunWith(JukitoRunner.class)
public class BindSpyInstanceTest {

    /**
     * Guice test module.
     */
    static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bindSpy(SimpleClass.class, new SimpleClass("foo")).in(TestScope.SINGLETON);
        }
    }

    static class SimpleClass {
        private String arg0;

        @SuppressWarnings("unused")
        SimpleClass() {
            this("default");
        }

        SimpleClass(String arg0) {
            this.arg0 = arg0;
        }

        String getVal() {
            return arg0;
        }
    }

    @Test
    public void testOneInvocation(SimpleClass simple) {
        String value = simple.getVal();

        assertEquals("foo", value);

        verify(simple).getVal();
    }

    @Test
    public void testNeverInvoked(SimpleClass simple) {
        verify(simple, never()).getVal();
    }
}
