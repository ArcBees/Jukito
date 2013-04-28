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

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Test that inheritance of test classes works correctly.
 *
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class ParentTestClassTest extends ParentTestClassBase {

    /**
     * Guice test module.
     */
    static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(DummyInterface.class).to(MyDummyClass.class).in(TestScope.SINGLETON);
        }
    }

    /**
     * This class should be injected in parent tests.
     */
    static class MyDummyClass implements DummyInterface {
        @Override
        public String getDummyValue() {
            return "DummyValue";
        }
    }

    @Test
    public void mockSingletonDefinedInParentShouldBeBoundAsAMock() {
        verify(mockSingletonDefinedInParent, never()).mockSingletonMethod();
    }

    @Test
    public void singletonDefinedInParentShouldBeBound(
            SingletonDefinedInParent singletonDefinedInParent) {
        assertEquals("SingletonDefinedInParentValue", singletonDefinedInParent.getValue());
    }

    @AfterClass
    public static void checkBookkeeper() {
        assertTrue(Bookkeeper.parentTestShouldRunExecuted);
    }
}
