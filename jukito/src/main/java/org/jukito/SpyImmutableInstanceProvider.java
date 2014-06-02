/**
 * Copyright 2014 ArcBees Inc.
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

import com.google.inject.Provider;

import org.mockito.Mockito;

/**
 * For use in classes where you want to create a spied instance, as in
 * {@link com.google.inject.binder.LinkedBindingBuilder#toInstance(T))},
 * except the instance is a spy.
 * <p/>
 * A new spy is returned each time this {@link Provider} is invoked, wrapping the
 * exact same instance class.
 * <p/>
 * <b>Important:</b> Spied object needs to be Immutable
 */
class SpyImmutableInstanceProvider<T> implements Provider<T> {
    private final T instance;

    /**
     * Create a new {@link Provider} instance for use in creating spies of
     * concrete instances.
     * <p/>
     * This instance should be immutable; if it is not, you risk polluting your tests as the underlying instance is the
     * same (even though it uses a different spy wrapper).
     *
     * @param instance The instance to be returned.
     */
    public SpyImmutableInstanceProvider(T instance) {
        this.instance = instance;
    }

    /**
     * Create a new spy of your bound instance.
     */
    @Override
    public T get() {
        return Mockito.spy(instance);
    }
}
