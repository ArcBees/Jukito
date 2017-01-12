/**
 * Copyright 2013 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.jukito;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;

/**
 * Test to ensure injection works well without a module.
 */
@RunWith(JukitoRunner.class)
public class NoModuleTest {

    interface MyMockSingleton {
        int dummy();
    }

    @TestMockSingleton
    interface MyAnnotatedMockSingleton {
        int dummy();
    }

    static class MySingleton {
    }

    @TestSingleton
    static class MyAnnotatedSingleton {
    }

    @TestEagerSingleton
    static class MyAnnotatedEagerSingleton {
    }

    @Test
    public void testMockSingleton(MyMockSingleton a, MyMockSingleton b) {
        assertSame(a, b);
        a.dummy();
        verify(a).dummy();
        verify(b).dummy();
    }

    @Test
    public void testAnnotatedMockSingleton(MyAnnotatedMockSingleton a,
            MyAnnotatedMockSingleton b) {
        assertSame(a, b);
        a.dummy();
        verify(a).dummy();
        verify(b).dummy();
    }

    @Test
    public void testSingleton(MySingleton a, MySingleton b) {
        assertSame(a, b);
    }

    @Test
    public void testAnnotatedSingleton(MyAnnotatedSingleton a, MyAnnotatedSingleton b) {
        assertSame(a, b);
    }

    @Test
    public void testAnnotatedEagerSingleton(MyAnnotatedEagerSingleton a,
            MyAnnotatedEagerSingleton b) {
        assertSame(a, b);
    }
}
