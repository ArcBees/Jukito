/*
 * Copyright 2017 ArcBees Inc.
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

/**
 * Sample Parent Test Class which binds 2 string instances for use by
 * the {@link ParentClassInnerClassModuleDiscoveryTest}.
 */
public class SampleParentTestClassWithInnerTestModule {

    /**
     * Sample JukitoModule which binds 2 String instances.
     * The Instances will be injected to an {@code @All} test and counted to verify that
     * this module is discovered by the JukitoRunner.
     */
    public static final class MyModule extends JukitoModule {
        @Override
        protected void configureTest() {
            bindManyInstances(String.class, "Hello", "World");
        }
    }
}
