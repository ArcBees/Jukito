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