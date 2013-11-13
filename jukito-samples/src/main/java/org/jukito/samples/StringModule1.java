package org.jukito.samples;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class StringModule1 extends AbstractModule {
    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("1")).toInstance("abc");
    }
}
