package org.jukito;

import org.mockito.Mockito;

import com.google.inject.Provider;

/**
 * For use in classes where you want to create a spied instance, as in
 * {@link com.google.inject.binder.LinkedBindingBinder#toInstance(T))},
 * except the instance is a spy.
 * 
 * A new spy is returned each time this {@link Provider} is invoked, wrapping the
 * exact same instance class. 
 * 
 * @author Jared Martin
 *
 */
class SpyInstanceProvider<T> implements Provider<T> {
	private final T instance;
	
	/**
	 * Create a new {@link Provider} instance for use in creating spies of
	 * concrete instances. 
	 * 
	 * @param instance The instance to be returned. This instance should be
	 *                 immutable; if it is not, you risk polluting your tests
	 *                 as the underlying instance is the same (even though it
	 *                 uses a different spy wrapper).
	 */
	public SpyInstanceProvider(T instance) {
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
