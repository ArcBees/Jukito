package org.jukito.samples;

import javax.inject.Inject;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.jukito.samples.modules.DieselLineModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.verify;


/**
 * A simple test with one mock (engine) and one real object (FordMustang)
 *
 * @author Przemysław Gałązka
 * @since 10-06-2013
 */
@RunWith(JukitoRunner.class)
public class FordMustangTest3 {

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

            // install yours module as you wish
            install(new DieselLineModule());

            // necessary if you want to verify interaction on real object
            bindSpy(DieselEngine.class);
        }
    }
}
