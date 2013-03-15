package org.jukito;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.jukito.UseModulesTest.Abc;
import org.jukito.UseModulesTest.AbcImpl;
import org.jukito.UseModulesTest.Def;
import org.jukito.UseModulesTest.DefImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;

@RunWith(JukitoRunner.class)
@UseModules({ AbcModule.class, DefModule.class})
public class UseModulesTest {
	interface Abc {}
	interface Def {}
	interface Ghj {}
	static class AbcImpl implements Abc {}
	static class DefImpl implements Def {}
	
	@Test
	public void testInjectionWithExternalModules(Abc abc, Def def) {
		assertTrue(abc instanceof AbcImpl);
		assertTrue(def instanceof DefImpl);
	}
	
	@Test
	public void testAutoMockingForMissingBindings(Ghj ghj) {
		assertNotNull(ghj);
		assertTrue(Mockito.mockingDetails(ghj).isMock());
	}
}

class AbcModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(Abc.class).to(AbcImpl.class);
	}	
}

class DefModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(Def.class).to(DefImpl.class);
	}
}
