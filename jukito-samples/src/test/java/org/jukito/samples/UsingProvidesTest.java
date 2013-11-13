package org.jukito.samples;

import javax.inject.Inject;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Provides;
import com.google.inject.name.Named;

import static org.junit.Assert.assertEquals;

@RunWith(JukitoRunner.class)
public class UsingProvidesTest {
    public static class MyModule extends JukitoModule {
        @Override
        protected void configureTest() {
        }

        @Provides
        @Named("1")
        String get1() {
            return "abc";
        }

        @Provides
        @Named("2")
        String get2() {
            return "def";
        }
    }

    @Inject
    @Named("1")
    String string1;
    @Inject
    @Named("2")
    String string2;

    @Test
    public void providesMethodWorks() {
        assertEquals("abc", string1);
        assertEquals("def", string2);
    }
}
