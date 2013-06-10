package org.jukito.samples;

import javax.inject.Inject;

import org.jukito.JukitoRunner;
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
public class FordMustangTest {

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
}
