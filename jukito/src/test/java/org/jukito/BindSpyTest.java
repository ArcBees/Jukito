/**
 * Copyright 2011 ArcBees Inc.
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

import com.google.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Test that binding spy works correctly
 */
@RunWith(JukitoRunner.class)
public class BindSpyTest {

    /**
     * Guice test module.
     */
    static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bindSpy(SimpleClass.class).in(TestScope.SINGLETON);
        }
    }

    interface CompositionMockA {
        String test();
    }

    interface CompositionMockB {
        String test();
    }

    static class SimpleClass {
        @Inject
        CompositionMockB mockB;

        private CompositionMockA mockA;

        @Inject
        SimpleClass(CompositionMockA mockA) {
            this.mockA = mockA;
        }

        String callTestMethodOnMock() {
            mockA.test();
            mockB.test();
            return "Default string";
        }
    }

    @Inject
    CompositionMockA mockA;
    @Inject
    CompositionMockA mockB;

    @Test
    public void testStubbingSpiedInstance(SimpleClass simpleClass) {
        // GIVEN
        doReturn("Mocked string").when(simpleClass).callTestMethodOnMock();

        // WHEN
        String result = simpleClass.callTestMethodOnMock();

        // THEN
        assertEquals("Mocked string", result);
        verify(mockA, never()).test();
        verify(mockB, never()).test();
    }

    @Test
    public void testNotStubbingSpiedInstance(SimpleClass simpleClass) {
        // WHEN
        String result = simpleClass.callTestMethodOnMock();

        // THEN
        verify(mockA).test();
        verify(mockB).test();
        assertEquals("Default string", result);
    }
}
