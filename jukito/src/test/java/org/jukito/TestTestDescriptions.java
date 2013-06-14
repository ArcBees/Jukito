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

/**
 * Simple test with @Description in place.
 */
@RunWith(JukitoRunner.class)
public class TestTestDescriptions {
    @Test
    @Description("some nice test description")
    public void testA() throws Exception {
        // Given

        // When

        // Then
    }

    @Test
    @Description("some nice ultra long test description, some nice ultra long test description," +
            "some nice ultra long test description, some nice ultra long test description")
    public void testB() throws Exception {
        // Given

        // When

        // Then
    }

    @Test
    public void testWithoutDescription() throws Exception {
        // Given

        // When

        // Then
    }
}
