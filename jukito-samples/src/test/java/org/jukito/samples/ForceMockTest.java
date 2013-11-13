package org.jukito.samples;

import javax.inject.Inject;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.verify;

@RunWith(JukitoRunner.class)
public class ForceMockTest {
    public static class MyModule extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(Engine.class).to(DieselEngine.class);
            forceMock(DieselEngine.class);
        }
    }

    @Inject
    Car car;
    @Inject
    Engine engine;

    @Test
    public void canVerifyBehaviorOnMockedConcreteClass() {
        car.turnKey();

        verify(engine).start();
    }
}
