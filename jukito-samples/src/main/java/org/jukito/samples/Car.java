package org.jukito.samples;

/**
 * @author Przemysław Gałązka
 * @since 10-06-2013
 */
public abstract class Car {

    private Engine engine;

    public Car(Engine engine) {
        this.engine = engine;
    }


    public void startEngine() {
        engine.initiateIgnition();
    }


    public Engine getEngine() {
        return engine;
    }
}
