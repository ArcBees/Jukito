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
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test various general behaviors.
 * 
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class GeneralTest {

  /**
   * Guice test module.
   */
  public static class Module extends JukitoModule {
    @Override
    protected void configureTest() {
      bindConstant().annotatedWith(OneHundred.class).to(100);
      bindConstant().annotatedWith(Names.named("200")).to(200);
      bindConstant().annotatedWith(Names.named("HelloWorld")).to("Hello World!");
      bindConstant().annotatedWith(Names.named("500L")).to(500L);
      bindConstant().annotatedWith(Names.named("true")).to(true);
      bindConstant().annotatedWith(Names.named("3.1415")).to(3.1415);
      bindConstant().annotatedWith(Names.named("2.718f")).to(2.718f);
      bindConstant().annotatedWith(Names.named("short8")).to((short) 8);
      bindConstant().annotatedWith(Names.named("'a'")).to('a');
      bindConstant().annotatedWith(Names.named("IntegerClass")).to(Integer.class);
      bindConstant().annotatedWith(Names.named("VALUE1")).to(MyEnum.VALUE1);
      bindConstant().annotatedWith(Names.named("VALUE2")).to(MyEnum.VALUE2);
      bind(MyInteger.class).annotatedWith(OneHundred.class).toInstance(new MyIntegerImpl(100));
      bind(MyInteger.class).annotatedWith(Names.named("200")).toInstance(new MyIntegerImpl(200));
      bind(Key.get(TestClass.class, Value3.class)).toInstance(new TestClass(MyEnum.VALUE3));
      bind(Key.get(TestClass.class, Names.named("VALUE2"))).to(TestClass.class).in(TestScope.SINGLETON);
      bind(new TypeLiteral<ParameterizedTestClass<Integer>>() { }).in(TestScope.SINGLETON);
      bind(new TypeLiteral<ParameterizedTestClass<Double>>() { }).to(ParameterizedTestClassDouble.class).in(TestScope.SINGLETON);
    }
  }
  
  interface MyInteger {
    int getValue();
  } 

  static class MyIntegerImpl implements MyInteger {
    private final int value;
    MyIntegerImpl(int value) {
      this.value = value;
    }
    @Override
    public int getValue() {
      return value;
    }
  } 
  
  static enum MyEnum {
    VALUE1,
    VALUE2,
    VALUE3
  }

  static class TestClass {
    private final MyEnum value;
    @Inject
    public TestClass(@Named("VALUE2") MyEnum value) {
      this.value = value;
    }
  }
  
  static class ParameterizedTestClass<T> {
    private final T value;
    @Inject
    public ParameterizedTestClass(@Named("200") T value) {
      this.value = value;
    }
  }
  
  static class ParameterizedTestClassDouble extends ParameterizedTestClass<Double> {
    @Inject
    public ParameterizedTestClassDouble() {
      super(10.0);
    }
  }

  static class TestClassWithMethodInjection {
    private int value;
    @Inject
    public TestClassWithMethodInjection(@OneHundred Integer value) {
      this.value = value;
    }
    @Inject
    public void setValue(@Named("200") Integer value) {
      this.value = value;
    }
  }
  
  interface NonBoundInterface {
    int getValue();
  }
  
  static class TestClassWithOptionalInjection {
    private int value;
    @Inject
    public TestClassWithOptionalInjection(@OneHundred Integer value) {
      this.value = value;
    }
    @Inject(optional = true)
    public void setValue(NonBoundInterface obj) {
      value = obj.getValue(); // Should never be called, NonBoundInterface should not be mocked
    }
  }
  
  @Test
  public void testConstantInjection(
      @OneHundred Integer oneHundred,
      @Named("200") Integer twoHundred,
      @Named("HelloWorld") String helloWorld,
      @Named("500L") long fiveHundred,
      @Named("3.1415") double pi,
      @Named("2.718f") float e,
      @Named("short8") short eight,
      @Named("'a'") char a,
      @SuppressWarnings("rawtypes")
      @Named("IntegerClass") Class integerClass,
      @Named("VALUE1") MyEnum value1) {
    assertEquals(100, (int) oneHundred);
    assertEquals(200, (int) twoHundred);
    assertEquals("Hello World!", helloWorld);
    assertEquals(500L, fiveHundred);
    assertEquals(3.1415, pi, 0.0000001);
    assertEquals(2.718f, e, 0.00001);
    assertEquals(8, eight);
    assertEquals('a', a);
    assertEquals(Integer.class, integerClass);
    assertEquals(MyEnum.VALUE1, value1);
  }

  @Test
  public void testInjectBoundWithKeys(
      @Value3 TestClass testClassValue3,
      @Named("VALUE2") TestClass testClassValue2,
      @OneHundred MyInteger testMyInteger100,
      @Named("200") MyInteger testMyInteger200) {
    assertEquals(MyEnum.VALUE3, testClassValue3.value);
    assertEquals(MyEnum.VALUE2, testClassValue2.value);
    assertEquals(100, testMyInteger100.getValue());
    assertEquals(200, testMyInteger200.getValue());
  }
  
  @Test
  public void testParameterizedInjection1(
      ParameterizedTestClass<Integer> testClass) {
    assertEquals(200, (int) testClass.value);
  }

  @Test
  public void testParameterizedInjection2(
      ParameterizedTestClass<Double> testClass) {
    assertEquals(10.0, (double) testClass.value, 0.0000001);
  }
  
  @Test
  public void testMethodInjection(
      TestClassWithMethodInjection testClass) {
    assertEquals(200, testClass.value);
  }
  
  @Test
  public void testOptionalInjection(
      TestClassWithOptionalInjection testClass) {
    assertEquals(100, testClass.value);
  }
  
}
