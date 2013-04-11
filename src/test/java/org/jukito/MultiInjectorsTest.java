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

import com.google.inject.Provides;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Przemysław Gałązka
 */
@RunWith(JukitoRunner.class)
public class MultiInjectorsTest {

    static int calls;

    @BeforeClass
    public static void setUp() throws Exception {
        calls = 0;
    }

    @Test
    public void shouldRunAsManyTimesAsManyInjectorsWereCreated(SomeCoreComponent coreComponent) throws Exception {
        coreComponent.run();
    }

    @AfterClass
    public static void tearDown() throws Exception {
//        assertEquals(calls, 2);
//        assertThat(calls, is(2));
    }

    /**
     * @author Przemysław Gałązka
     */
    public static class A extends JukitoModule {

        @Provides
        SomeCoreComponent createCalculator(@All EnvironmentDependentAdapter adapter) {
            return new SomeCoreComponent(adapter);
        }

        @Override
        protected void configureTest() {
            bindManyInstances(EnvironmentDependentAdapter.class,
                    new EnvironmentDependentAdapter() {
                        @Override
                        public void hello() {
                            ++calls;
                        }
                    },
                    new EnvironmentDependentAdapter() {
                        @Override
                        public void hello() {
                            ++calls;
                        }
                    }
            );
        }
    }
}
