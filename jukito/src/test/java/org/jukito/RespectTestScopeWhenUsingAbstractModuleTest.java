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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for respecting {@literal @}{@link org.jukito.TestSingleton}
 * when user modules subclass AbstractModule.
 */
@RunWith(JukitoRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RespectTestScopeWhenUsingAbstractModuleTest {

    @Inject
    SomeTestClass someTestClassOne;

    @Inject
    SomeTestClass someTestClassTwo;

    @Test
    public void shouldRespectTestSingletonAnnotationA() throws Exception {
        //-------------------- GIVEN -------------------------------------------------------------------

        //-------------------- WHEN --------------------------------------------------------------------
        // calls for purpose of test shouldRespectTestSingletonsB
        someTestClassOne.crazyMethod();
        someTestClassOne.crazyMethod();
        someTestClassOne.crazyMethod();

        //-------------------- THEN --------------------------------------------------------------------
        assertEquals(someTestClassOne, someTestClassTwo);
    }

    @Test
    public void shouldRespectTestSingletonsB() throws Exception {
        //-------------------- GIVEN -------------------------------------------------------------------

        //-------------------- WHEN --------------------------------------------------------------------
        someTestClassOne.crazyMethod();

        //-------------------- THEN --------------------------------------------------------------------
        // verify if mock has been reset and thus only one call is registered
        verify(someTestClassOne, times(1)).crazyMethod();
    }

    @Test
    public void shouldRespectTestSingletonsC() throws Exception {
        //-------------------- GIVEN -------------------------------------------------------------------

        //-------------------- WHEN --------------------------------------------------------------------
        someTestClassOne.crazyMethod();
        someTestClassTwo.crazyMethod();

        //-------------------- THEN --------------------------------------------------------------------
        verify(someTestClassOne, times(2)).crazyMethod();
    }

    public static class A extends JukitoModule {
        @Override
        protected void configureTest() {
            install(new ModuleWithProvidesMethods());
        }
    }
}
