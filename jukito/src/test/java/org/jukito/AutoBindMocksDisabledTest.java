package org.jukito;

import com.google.inject.PrivateModule;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.inject.Inject;

class MyModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(ExposedClass.class);
        bind(SomeInterface.class).to(NotAMock.class);
        expose(ExposedClass.class);
    }
}

interface SomeInterface {
    void doSomething();
}

class NotAMock implements SomeInterface {

    @Override
    public void doSomething() {

    }
}

class ExposedClass {

    private SomeInterface instance;

    @Inject
    ExposedClass(final SomeInterface instance) {
        this.instance = instance;
    }

    SomeInterface getInstance() {
        return instance;
    }
}

/**
 * NOTE: If autoBindMocks is true, this test will fail because SomeInterface will be auto bound
 * to a mock and the binding will conflict with private module's binding
 */
@RunWith(JukitoRunner.class)
@UseModules(value = MyModule.class, autoBindMocks = false)
public class AutoBindMocksDisabledTest {

    @Test
    public void testSomething(final ExposedClass clazz) throws Exception {
        Assert.assertFalse(Mockito.mockingDetails(clazz).isMock());
        Assert.assertFalse(Mockito.mockingDetails(clazz.getInstance()).isMock());
    }
}
