package org.jukito;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

class ParentTestClass {

    public static final class MyModule extends JukitoModule {

        @Override
        protected void configureTest() {
            bindManyInstances(String.class, "Hello", "World");
        }
    }
}

@RunWith(JukitoRunner.class)
public class ParentClassInnerClassModuleDiscoveryTest extends ParentTestClass {

    private static final AtomicInteger numberOfTestRuns = new AtomicInteger(0);

    @AfterClass
    public static void afterClass() throws Exception {
        Assert.assertEquals(2, numberOfTestRuns.get());
    }

    @Test
    public void testSomething(@All final String bindingFromParentClassInnerModule) throws Exception {
        numberOfTestRuns.incrementAndGet();
    }
}
