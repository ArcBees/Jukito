package org.jukito;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.Module;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseModules {
  Class<? extends Module>[] value();
}
