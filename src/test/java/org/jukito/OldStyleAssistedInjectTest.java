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
 * Test that make sure old-style Guice 2.0 assisted injection is supported.
 *
 * @author Christian Goudreau
 * @author Philippe Beaudoin
 */
@SuppressWarnings("deprecation")
@RunWith(JukitoRunner.class)
public class OldStyleAssistedInjectTest {
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
          return "An amount of 10.00 ";
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
  // Class InjectedClass should be bound automatically
  // because it is a dependency of RealPaymentAmount
  static class InjectedClass {
    @Inject @Named("moneySymbol") String moneySymbol;
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
        return "xxxxxxx " + injectedClass.moneySymbol;
      }
      return amount.toString() + injectedClass.moneySymbol;
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
    assertEquals("An amount of 10.00 $", payment.getPayment());
  }
}