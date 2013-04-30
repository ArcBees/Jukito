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

import com.google.inject.AbstractModule;

import org.jukito.UseModulesTest.Abc;
import org.jukito.UseModulesTest.AbcImpl;
import org.jukito.UseModulesTest.Def;
import org.jukito.UseModulesTest.DefImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JukitoRunner.class)
@UseModules({AbcModule.class, DefModule.class})
public class UseModulesTest {
    interface Abc {
    }

    interface Def {
    }

    interface Ghj {
    }

    static class AbcImpl implements Abc {
    }

    static class DefImpl implements Def {
    }

    @Test
    public void testInjectionWithExternalModules(Abc abc, Def def) {
        assertTrue(abc instanceof AbcImpl);
        assertTrue(def instanceof DefImpl);
    }

    @Test
    public void testAutoMockingForMissingBindings(Ghj ghj) {
        assertNotNull(ghj);
        assertTrue(Mockito.mockingDetails(ghj).isMock());
    }
}

class AbcModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Abc.class).to(AbcImpl.class);
    }
}

class DefModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Def.class).to(DefImpl.class);
    }
}
