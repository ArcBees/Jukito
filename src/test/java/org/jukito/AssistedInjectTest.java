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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Test that make sure assisted injection is supported.
 * 
 * @author Christian Goudreau
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class AssistedInjectTest {
  /**
   * Guice test module.
   */
  public static class Module extends JukitoModule {
    @Override
    protected void configureTest() {
      bindConstant().annotatedWith(Names.named("moneySymbol")).to("$");
      bindNamed(PaymentFactory.class, "factory1").toProvider(
          FactoryProvider.newFactory(PaymentFactory.class, RealPayment1.class));
      bindNamed(PaymentFactory.class, "factory2").toProvider(
          FactoryProvider.newFactory(PaymentFactory.class, RealPayment2.class));
      bind(PaymentAmountFactory.class).toProvider(
          FactoryProvider.newFactory(PaymentAmountFactory.class, RealPaymentAmount.class));
      bind(Amount.class).toInstance(new Amount() { 
        public String toString() {
          return "An amount of 10.00 dollars.";
        }
      });
    }
  }

  interface PaymentFactory {
    Payment create(int amount);
  }
  
  interface PaymentAmountFactory {
    Payment create(Amount amount);
  }

  interface Payment {
    String getPayment();
  }
  
  static class RealPayment1 implements Payment {
    private final String moneySymbol;
    private final int amount;
    
    @Inject
    public RealPayment1(@Named("moneySymbol") String moneySymbol, @Assisted int amount) {
      this.moneySymbol = moneySymbol;
      this.amount = amount;
    }

    @Override
    public String getPayment() {
      return Integer.toString(amount) + ".00" + moneySymbol;
    }
  }
  
  static class RealPayment2 implements Payment {
    private final int amount;

    @Inject
    public RealPayment2(@Assisted int amount) {
      this.amount = amount;
    }
    
    @Override
    public String getPayment() {
      return Integer.toString(amount) + " dollars";
    }
  }

  static interface Amount {
    String toString();
  }

  // Interface Configuration should be mocked because it is a dependency 
  // of RealPaymentAmout. The mocked version of {@ocde shouldAlwaysHideAmounts()}
  // will always return {@code false}.
  static interface Configuration {
    boolean shouldAlwaysHideAmounts();
  }

  // Class InjectedClass should be bound automatically as TestSingleton
  // because it is a dependency of RealPaymentAmount
  static class InjectedClass {
  }
  
  static class RealPaymentAmount implements Payment {
    private final Configuration configuration;
    private final InjectedClass injectedClass;
    private final Amount amount;

    @Inject
    public RealPaymentAmount(Configuration configuration, 
        InjectedClass injectedClass, 
        @Assisted Amount amount) {
      this.configuration = configuration;
      this.injectedClass = injectedClass;
      this.amount = amount;
    }
    
    @Override
    public String getPayment() {
      if (configuration.shouldAlwaysHideAmounts()) {
        return "xxxxxxx";
      }
      return amount.toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((injectedClass == null) ? 0 : injectedClass.hashCode());
      return result;
    }

    /* Checks only injectedClass, to ensure injectedClass is a singleton.
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      RealPaymentAmount other = (RealPaymentAmount) obj;
      if (injectedClass == null) {
        if (other.injectedClass != null) {
          return false;
        }
      } else if (!injectedClass.equals(other.injectedClass)) {
        return false;
      }
      return true;
    }
  }
  
  @Inject @Named("factory1") PaymentFactory factory1;
  
  @Test
  public void shouldInjectFactoryInClass() {
    // WHEN
    Payment payment = factory1.create(20);
    
    // THEN
    assertEquals("20.00$", payment.getPayment());
  }

  @Test
  public void shouldInjectFactoryAsParameter(@Named("factory2") PaymentFactory factory2) {
    // WHEN
    Payment payment = factory2.create(30);
    
    // THEN
    assertEquals("30 dollars", payment.getPayment());
  }

  @Test
  public void shouldInjectFactoryWithInterfacesAsParameter(
      PaymentAmountFactory factoryString,
      Amount amount) {
    // WHEN
    Payment payment = factoryString.create(amount);
    
    // THEN
    assertEquals("An amount of 10.00 dollars.", payment.getPayment());
  }

  @Test
  public void shouldInjectFactoryWithTestSingletonAsParameter(
      PaymentAmountFactory factoryString,
      Amount amount) {
    // WHEN
    Payment payment1 = factoryString.create(amount);
    Payment payment2 = factoryString.create(amount);
    
    // THEN
    assertEquals(payment1, payment2);
  }
}
