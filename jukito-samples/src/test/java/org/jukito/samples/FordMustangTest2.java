package org.jukito.samples;

import javax.inject.Inject;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.verify;


/**
 * A simple test with one real DOC (binding in test)
 *
 * @author Przemysław Gałązka
 * @since 10-06-2013
 */
@RunWith(JukitoRunner.class)
public class FordMustangTest2 {

    @Inject
    FordMustang sut;

    @Test
    public void shouldInitiateIgnitionWhenCarStart() throws Exception {
        //-------------------- GIVEN -------------------------------------------------------------------

        //-------------------- WHEN --------------------------------------------------------------------
        sut.startEngine();

        //-------------------- THEN --------------------------------------------------------------------
        verify(sut.getEngine()).initiateIgnition();
    }

    public static class A extends JukitoModule {
        @Override
        protected void configureTest() {

            // Diesel in Mustang, yeaah I know :)
            bind(Engine.class).to(DieselEngine.class);

            // necessary if you want to verify interaction on real object
            bindSpy(DieselEngine.class);
        }
    }
}
