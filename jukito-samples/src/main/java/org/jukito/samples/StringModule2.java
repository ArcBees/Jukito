package org.jukito.samples;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class StringModule2 extends AbstractModule {
    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("2")).toInstance("def");
    }
}
