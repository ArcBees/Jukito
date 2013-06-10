package org.jukito.samples.modules;

import org.jukito.samples.DieselEngine;
import org.jukito.samples.Engine;

import com.google.inject.AbstractModule;

/**
 * @author Przemysław Gałązka
 * @since 10-06-2013
 */
public class DieselLineModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Engine.class).to(DieselEngine.class);
    }
}
