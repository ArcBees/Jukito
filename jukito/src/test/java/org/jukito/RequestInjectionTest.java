/**
 * Copyright 2014 ArcBees Inc.
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

import jakarta.inject.Inject;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockingDetails;

/**
 * Test class for request injection.
 */
@RunWith(JukitoRunner.class)
public class RequestInjectionTest {
    interface Dummy {
    }

    static class RequestInjection {
        @Inject
        Dummy dummy;

        public Dummy getDummy() {
            return dummy;
        }
    }

    static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            requestInjection(RequestInjection.class);
        }
    }

    // SUT
    @Inject
    RequestInjection requestInjection;

    @Test
    public void dummyShouldBeMocked() {
        Dummy dummy = requestInjection.getDummy();

        assertTrue(mockingDetails(dummy).isMock());
    }
}
