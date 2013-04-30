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

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.verify;

@RunWith(JukitoRunner.class)
public class RespectProvidesAnnotationInModuleTest {

    @Test
    public void shouldRespectProvidesAnnotationUsedInModule(SomeTestClass someTestClass) throws Exception {
        //-------------------- GIVEN -------------------------------------------------------------------

        //-------------------- WHEN --------------------------------------------------------------------

        //-------------------- THEN --------------------------------------------------------------------
        // injected object should be created by factory method
        // defined in  custom module  ModuleWithProvidesMethods.
        // Init method should be called from factory method
        verify(someTestClass).someInitMethod();
    }

    /**
     * @author Przemysław Gałązka
     * @since 05-04-2013
     */
    public static class A extends JukitoModule {
        @Override
        protected void configureTest() {
            install(new ModuleWithProvidesMethods());
        }
    }
}
