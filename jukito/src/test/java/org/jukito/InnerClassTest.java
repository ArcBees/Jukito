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

import static org.junit.Assert.*;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;

/**
 * Test to ensure that injecting inner classes throw a ConfigurationException, instead
 * of simply injecting a mock. Additionally, test that injecting static inner classes
 * still work properly.
 */
@RunWith(JukitoRunner.class)
public class InnerClassTest {
    public static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(String.class).toInstance("hello world!");
        }
    }
    
    @Test(expected=ConfigurationException.class)
    public void testInnerClass(InnerClass f) {
        assertEquals("hello world!", f.toString());
    }
    
    @Test
    public void testStaticInnerClass(StaticInnerClass f) {
        assertEquals("hello world!", f.toString());
    }

    public class InnerClass {
        @Inject String test;

        public String toString() {
            return test;
        }
    }
    
    public static class StaticInnerClass {
        @Inject String test;
        
        public String toString() {
            return test;
        }
    }
}
