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

import com.google.inject.Provides;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockingDetails;

/**
 * Test class for request static injection.
 */
@RunWith(JukitoRunner.class)
public class RequestStaticInjectionTest {
    interface Dummy {
    }

    static class RequestStaticInjectionA {
        @Inject
        @Named("a")
        static Dummy DUMMY;
    }

    static class RequestStaticInjectionB {
        @Inject
        static Dummy DUMMY;
    }

    static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            requestStaticInjection(RequestStaticInjectionA.class);
            requestStaticInjection(RequestStaticInjectionB.class);
        }

        @Provides
        @Named("a")
        Dummy createDummy() {
            return new Dummy() {
            };
        }
    }

    @Test
    public void dummyShouldNotBeMocked() {
        Dummy dummy = RequestStaticInjectionA.DUMMY;

        assertFalse(mockingDetails(dummy).isMock());

        assertNotNull(dummy);
    }

    @Test
    public void dummyShouldBeMocked() {
        Dummy dummy = RequestStaticInjectionB.DUMMY;

        assertTrue(mockingDetails(dummy).isMock());
    }
}
