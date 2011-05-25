/**
 * Copyright 2010 ArcBees Inc.
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

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.name.Named;

/**
 * Test that annotated concrete classes can be correctly bound.
 * See http://code.google.com/p/jukito/issues/detail?id=12
 *
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class BindAnnotatedConcreteClassesTest {

  /**
   * Guice test module.
   */
  static class Module extends JukitoModule {
    @Override
    protected void configureTest() {
      bindNamed(ConcreteClass.class, "a").to(ConcreteClass.class).in(TestSingleton.class);
      bindNamed(ConcreteClass.class, "b").to(ConcreteClass.class).in(TestSingleton.class);
      bindNamed(ConcreteClass.class, "c").to(SubConcreteClass.class);
      bindNamed(ConcreteClass.class, "d").to(SubConcreteClass.class);
      bind(SubConcreteClass.class).in(TestSingleton.class);
      bind(SubSubConcreteClass.class);
    }
  }

  static class ConcreteClass {
  }

  static class SubConcreteClass extends ConcreteClass {
  }

  static class SubSubConcreteClass extends ConcreteClass {
  }

  @Test
  public void testConcreteClassBoundToDifferentSingletons(@Named("a") ConcreteClass a,
      @Named("b") ConcreteClass b) {
    // THEN
    assertNotSame(a, b);
  }

  @Test
  public void testConcreteClassBoundToSameSingleton(@Named("c") ConcreteClass c,
      @Named("d") ConcreteClass d) {
    // THEN
    assertSame(c, d);
  }

  @Test
  public void testConcreteClassNoBoundAsSingleton(SubSubConcreteClass instance1,
      SubSubConcreteClass instance2) {
    // THEN
    assertNotSame(instance1, instance2);
  }
}
