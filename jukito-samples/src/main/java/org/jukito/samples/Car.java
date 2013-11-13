package org.jukito.samples;

import javax.inject.Inject;

public class Car {
    private final Engine engine;

    @Inject
    Car(Engine engine) {
        this.engine = engine;
    }

    public void turnKey() {
        engine.start();
    }
}
