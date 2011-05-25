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

import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.Set;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;

/**
 * For use in test cases where an {@link Provider} is required to provide an
 * object and the test case needs to provide a spy of the object.
 * <p />
 * A new object is returned each the the provider is invoked, unless the object
 * is bound as a {@link TestScope#SINGLETON} or {@link TestScope#EAGER_SINGLETON}.
 * <p />
 * Depends on Mockito.
 *
 * @author Philippe Beaudoin
 *
 * @param <T> The class to provide.
 */
class SpyProvider<T> implements Provider<T>, HasDependencies {

  private final Provider<T> rawProvider;
  private final Set<Dependency<?>> dependencies;

  /**
   * Construct a {@link Provider} that will return spied instances of objects
   * of the specified types. You should not call this directly, instead use
   * {@link TestModule#bindSpy(Class)} or {@link TestModule#bindSpy(com.google.inject.TypeLiteral)}.
   *
   * @param rawProvider The test class, running with {@link JukitoRunner}.
   * @param relayingKey The key of the binding used to relay to the real class. This should usually
   *                    be the key of a {code toConstructor} binding. Internally, Jukito uses the
   *                    {@link JukitoInternal} annotation to distinguish this binding.
   */
  public SpyProvider(Provider<T> rawProvider, Key<T> relayingKey) {
    this.rawProvider = rawProvider;
    dependencies = Collections.<Dependency<?>>singleton(Dependency.get(relayingKey));
  }

  @Override
  public T get() {
    return spy(rawProvider.get());
  }

  @Override
  public Set<Dependency<?>> getDependencies() {
    return dependencies;
  }
}
