package org.jukito.samples;

import javax.inject.Inject;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.jukito.TestSingleton;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.verify;

@RunWith(JukitoRunner.class)
public class BindSpyTest {
    public static class MyModule extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(Engine.class).to(DieselEngine.class);
            bindSpy(DieselEngine.class).in(TestSingleton.class);
        }
    }

    @Inject
    Car car;
    @Inject
    Engine engine;

    @Test
    public void canVerifyBehaviorOnSpy() {
        car.turnKey();

        verify(engine).start();
    }
}
