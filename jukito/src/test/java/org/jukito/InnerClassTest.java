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

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Test to ensure that injecting inner classes throw a ConfigurationException, instead
 * of simply injecting a mock. Additionally, test that injecting static inner classes
 * still work properly.
 */
@RunWith(JukitoRunner.class)
public class InnerClassTest {
    /**
     * Test module, just bind anything to make sure regular injections still work properly.
     */
    public static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(String.class).toInstance("hello world!");
        }
    }

    /**
     * Dummy inner class with a single inject.
     */
    class InnerClass {
        @Inject
        String test;

        public String toString() {
            return test;
        }
    }

    /**
     * Dummy static inner class with a single inject.
     */
    static class StaticInnerClass {
        @Inject
        String test;

        public String toString() {
            return test;
        }
    }

    /**
     * Verify that when you try to inject an inner class, a ConfigurationException is thrown.
     *
     * @param klass
     */
    @Test(expected = ConfigurationException.class)
    public void testInnerClass(InnerClass klass) {
        assertEquals("hello world!", klass.toString());
    }

    /**
     * Verify that when you try to inject a static inner class, everything works properly.
     *
     * @param klass
     */
    @Test
    public void testStaticInnerClass(StaticInnerClass klass) {
        assertEquals("hello world!", klass.toString());
    }
}
