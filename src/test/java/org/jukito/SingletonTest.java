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

import com.google.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test that the various flavours of singletons work correctly.
 * 
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class SingletonTest {

  /**
   * Guice test module.
   */
  public static class Module extends JukitoModule {
    @Override
    protected void configureTest() {
      bind(MyEagerSingleton.class).asEagerSingleton();
    }
  }
  
  /**
   */
  @TestSingleton
  public static class Registry {
    public int registrationCount;
    public void register() { 
      registrationCount++;
    }
  }

  /**
   * This class keeps track of what happens in all the tests run in this
   * class. It's used to make sure all expected tests are called.
   */
  private static class Bookkeeper {
    static int numberOfTimesTestEagerSingletonIsInstantiated;
    static int numberOfTimesTestSingletonIsInstantiated;
    static int numberOfTimesEagerSingletonIsInstantiated;
  }

  /**
   * This should be instantiated once for the entire test class.
   */
  static class MyEagerSingleton {
    @Inject
    public MyEagerSingleton(Registry registry) {
      registry.register();
      Bookkeeper.numberOfTimesEagerSingletonIsInstantiated++;
    }
  }
  
  /**
   * This should automatically register before each test.
   */
  @TestEagerSingleton
  static class MyTestEagerSingleton {
    @Inject
    public MyTestEagerSingleton(Registry registry) {
      registry.register();
      Bookkeeper.numberOfTimesTestEagerSingletonIsInstantiated++;
    }
  }
  
  /**
   * This should register only in tests where it is injected.
   */
  @TestSingleton
  static class MyTestSingleton {
    @Inject
    public MyTestSingleton(Registry registry) {
      registry.register();
      Bookkeeper.numberOfTimesTestSingletonIsInstantiated++;
    }
  }

  /**
   * This should be different from one test to the next.
   */
  @TestMockSingleton
  interface MyTestMockSingleton {
    void dummy();
  }

  @Inject Registry registry;
  
  @Test
  public void onlyEagerSingletonShouldBeRegistered() {
    assertEquals(1, registry.registrationCount);
  }
  
  @Test
  public void bothSingletonsShouldBeRegistered(MyTestSingleton myTestSingleton) {
    assertEquals(2, registry.registrationCount);
  }

  @Test
  public void injectionOfMockShouldBeADifferentObject1(MyTestMockSingleton myTestMockSingleton) {
    myTestMockSingleton.dummy();
    verify(myTestMockSingleton).dummy();
  }
  
  @Test
  public void injectionOfMockShouldBeADifferentObject2(MyTestMockSingleton myTestMockSingleton) {
    myTestMockSingleton.dummy();
    verify(myTestMockSingleton).dummy();
  }
  
  @AfterClass
  public static void verifyNumberOfInstantiations() {
    assertEquals(4, Bookkeeper.numberOfTimesTestEagerSingletonIsInstantiated);
    assertEquals(1, Bookkeeper.numberOfTimesTestSingletonIsInstantiated);
    assertEquals(1, Bookkeeper.numberOfTimesEagerSingletonIsInstantiated);
  }

}
