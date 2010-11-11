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

package com.google.inject.assistedinject;

import java.lang.reflect.Field;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/**
 * This class gives us access to a few hidden methods in the assisted inject
 * extension.
 * 
 * @author Philippe Beaudoin
 */
public class AssistedInjectHelper {
  
  /**
   * Checks that the specified class type is a factory generated
   * via {@link FactoryProvider#newFactory(Class, Class)}.
   * 
   * @param klass The class to check. 
   * @return {@code true} if the class is a factory, {@code false} otherwise.
   */
  public static boolean isFactory(Class<?> klass) {
    return FactoryProvider2.class.isAssignableFrom(klass);
  }
  
  /**
   * This method finds the concrete type provided by the factory provider passed
   * as a parameter. This will only work if the parameter is a factory provider
   * generated via {@link FactoryProvider#newFactory(Class, Class)}. To
   * ensure this is true use {@link #isFactory(Class)}.
   * 
   * @param <T> The provided type
   * @param factoryProvider The factory provider to inspect.
   * @return A {@link TypeLiteral} identifying the concrete provided type.
   */
  @SuppressWarnings("unchecked")
  public static <T> TypeLiteral<T> getProvidedType(Provider<T> factoryProvider) {
    FactoryProvider2<?> factoryProviderConcrete = (FactoryProvider2<?>) factoryProvider;
    // TODO: Found no way to access the private variable producedType. It is needed in order
    //       to identify the dependencies that may need to be mocked.
    try {      
      Field field = factoryProviderConcrete.getClass().getDeclaredField("producedType");
      field.setAccessible(true);
      Key<T> key = (Key<T>) field.get(factoryProviderConcrete);
      return key.getTypeLiteral();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }  
}
