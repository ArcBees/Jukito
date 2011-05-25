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
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Tests that new Guice 3.0 assisted injection works in Jukito.
 *
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class AssistedInjectTest {

  /**
   * Guice test module.
   */
  static class Module extends JukitoModule {
    @Override
    protected void configureTest() {
      install(new FactoryModuleBuilder().implement(Payment.class, RealPayment.class)
          .build(PaymentFactory.class));
    }
  }

  interface PaymentFactory {
    Payment create(Date startDate, int amount);
  }

  interface Payment {
    String format();
  }

  static class RealPayment implements Payment {
    private final Date date;
    private final int amount;
    private final LocaleInfo localeInfo;
    @Inject
    public RealPayment(@Assisted Date date, @Assisted int amount, LocaleInfo localeInfo) {
      this.date = date;
      this.amount = amount;
      this.localeInfo = localeInfo;
    }
    @Override
    public String format() {
      String result = "Paid " + Integer.toString(amount);
      if (localeInfo.isMoneySignBefore()) {
        result = localeInfo.getMoneySign() + result;
      } else {
        result += localeInfo.getMoneySign();
      }
      SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
      result += " on " + formatter.format(date);

      return result;
    }
  }

  interface LocaleInfo {
    String getMoneySign();
    boolean isMoneySignBefore();
  }

  @Before
  public void setUp(LocaleInfo localeInfo) {
    when(localeInfo.getMoneySign()).thenReturn("$");
    when(localeInfo.isMoneySignBefore()).thenReturn(false);
  }

  @Test
  public void testFactory(PaymentFactory factory) {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
    calendar.set(2011, 4, 24); // Month is 0-based
    Payment payment = factory.create(calendar.getTime(), 50);
    assertEquals("Paid 50$ on 05/24/2011", payment.format());
  }

}
