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

package org.jukito.samples;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.jukito.samples.modules.DieselLineModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.mockito.Mockito.verify;

/**
 * A simple test with one real DOC (binding via module installation).
 *
 * @author Przemysław Gałązka
 * @since 10-06-2013
 */
@RunWith(JukitoRunner.class)
public class FordMustangTest3 {

    @Inject
    FordMustang sut;

    @Test
    public void shouldInitiateIgnitionWhenCarStart() throws Exception {
        //-------------------- GIVEN -------------------------------------------------------------------

        //-------------------- WHEN --------------------------------------------------------------------
        sut.startEngine();

        //-------------------- THEN --------------------------------------------------------------------
        verify(sut.getEngine()).initiateIgnition();
    }

    /**
     * JukitoModule.
     */
    public static class A extends JukitoModule {
        @Override
        protected void configureTest() {

            // install yours module as you wish
            install(new DieselLineModule());

            // necessary if you want to verify interaction on real object
            bindSpy(DieselEngine.class);
        }
    }
}
