package org.jukito.samples;

import javax.inject.Inject;

import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.verify;

@RunWith(JukitoRunner.class)
public class SimpleTest {
    @Inject
    Car car;
    @Inject
    Engine engine;

    @Test
    public void fieldInjectionTest() {
        car.turnKey();

        verify(engine).start();
    }

    @Test
    public void methodInjectionTest(Engine sameEngineInstance) {
        car.turnKey();

        verify(sameEngineInstance).start();
    }
}
