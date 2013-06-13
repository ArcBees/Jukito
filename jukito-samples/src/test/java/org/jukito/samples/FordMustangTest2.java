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

package org.jukito.samples;

import javax.inject.Inject;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.verify;

/**
 * A simple test with one real DOC (binding in test)
 */
@RunWith(JukitoRunner.class)
public class FordMustangTest2 {
    @Inject
    FordMustang sut;

    @Test
    public void shouldInitiateIgnitionWhenCarStart() throws Exception {
        // Given

        // Then
        sut.startEngine();

        // Then
        verify(sut.getEngine()).initiateIgnition();
    }

    public static class A extends JukitoModule {
        @Override
        protected void configureTest() {

            // Diesel in Mustang, yeaah I know :)
            bind(Engine.class).to(DieselEngine.class);

            // necessary if you want to verify interaction on real object
            bindSpy(DieselEngine.class);
        }
    }
}
