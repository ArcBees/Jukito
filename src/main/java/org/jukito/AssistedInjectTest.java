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

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryProvider;

/**
 * Test that make sure assisted injection is supported.
 * 
 * @author Christian Goudreau
 */
@RunWith(JukitoRunner.class)
public class AssistedInjectTest {
  /**
   * Guice test module.
   */
  public static class Module extends JukitoModule {
    @Override
    protected void configureTest() {
      bind(Payment.class).to(RealPayment1.class);
      bind(PaymentFactory1.class).toProvider(
          FactoryProvider.newFactory(PaymentFactory1.class, Payment.class));
      bind(PaymentFactory2.class).toProvider(
          FactoryProvider.newFactory(PaymentFactory2.class, Payment.class));
    }
  }

  interface PaymentFactory1 {
    RealPayment1 create(Amount amount);
  }
  
  interface PaymentFactory2 {
    RealPayment1 create(String amount);
  }

  interface Payment {
  }
  
  interface Amount {
  }

  static class RealPayment1 implements Payment {
    @Inject
    public RealPayment1(@Assisted Amount amount) {
    }
  }
  
  static class RealPayment2 implements Payment {
    @Inject
    public RealPayment2(@Assisted String amount) {
    }
  }
  
  // SUT
  @Inject
  PaymentFactory1 paymentFactory1;
  @Inject
  PaymentFactory2 paymentFactory2;
  
  @Test
  public void shouldntFailToLoad() {
    assert true;
  }
}
