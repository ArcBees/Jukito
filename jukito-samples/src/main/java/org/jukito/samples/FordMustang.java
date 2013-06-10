package org.jukito.samples;

import javax.inject.Inject;

/**
 * @author Przemysław Gałązka
 * @since 10-06-2013
 */
public class FordMustang extends Car {

    @Inject
    public FordMustang(Engine engine) {
        super(engine);
    }
}
