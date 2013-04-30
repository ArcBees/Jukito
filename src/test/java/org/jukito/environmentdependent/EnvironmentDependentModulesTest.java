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
package org.jukito.environmentdependent;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import org.jukito.EDRunner;
import org.jukito.EnvironmentDependentComponent;
import org.jukito.EnvironmentDependentModules;
import org.jukito.SomeCoreComponent;
import org.jukito.UseModules;
import org.jukito.environmentdependent.EnvironmentDependentModulesTest.SomeCoreModule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test is run as many times as many Environment Dependent Modules you declare.
 * Every Environment Dependent Module is installed in separated Injector
 * with default core modules you declare in @UseModules
 */
@EnvironmentDependentModules({MobileModule.class, DesktopModule.class, TabletModule.class})
@UseModules(SomeCoreModule.class)
@RunWith(EDRunner.class)
public class EnvironmentDependentModulesTest {
    @Test
    public void shouldRunAsManyTimesAsManyInjectorsWereCreated(SomeCoreComponent coreComponent) throws Exception {
        coreComponent.run();
    }

    public static class SomeCoreModule extends AbstractModule {
        @Provides
        SomeCoreComponent createCalculator(EnvironmentDependentComponent dependentComponent) {
            return new SomeCoreComponent(dependentComponent);
        }

        @Override
        protected void configure() {
        }
    }
}
