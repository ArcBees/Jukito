package org.jukito.samples.modules;

import org.jukito.samples.Engine;
import org.jukito.samples.PetrolEngine;

import com.google.inject.AbstractModule;

/**
 * @author Przemysław Gałązka
 * @since 10-06-2013
 */
public class PetrolLineModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Engine.class).to(PetrolEngine.class);
    }
}
