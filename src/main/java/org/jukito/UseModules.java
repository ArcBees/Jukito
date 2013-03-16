package org.jukito;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.Module;

/**
 * This annotation can be used on a test class together with
 * {@code @RunWith(JukitoRunner.class)} to use the bindings contained
 * in the specified modules for the test.
 * <p/>
 * Example:
 * <pre>
 * {@literal @}RunWith(JukitoRunner.class)
 * {@literal @}UseModules({ FooModule.class, BarModule.class}
 * public class MyTest {
 *   // Tests methods  
 * }</pre>
 * 
 * The example is equivalent to the following <i>inner static module
 * class</i> approach.
 * <pre>
 * {@literal @}RunWith(JukitoRunner.class)
 * public class MyTest {
 *   static class Module extends JukitoModule {
 *     {@literal @}Override
 *     protected void configureTest() {
 *       install(new FooModule());
 *       install(new BarModule());
 *     }
 *   }
 *   // Test methods
 * }</pre>
 * 
 * @author Julian Lettner
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseModules {
  Class<? extends Module>[] value();
}
