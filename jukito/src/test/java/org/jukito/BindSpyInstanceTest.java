package org.jukito;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Test that binding Spy Instances work correctly
 * 
 * @author Jared Martin
 *
 */
@RunWith(JukitoRunner.class)
public class BindSpyInstanceTest {
    
    /**
     * Guice test module
     */
	static class Module extends JukitoModule {		
		@Override
		protected void configureTest() {
			bindSpy(SimpleClass.class, new SimpleClass("foo")).in(TestScope.SINGLETON);
		}
	}
	
	static class SimpleClass {
		private String arg0;
		
		SimpleClass() {
		    this("default");
		}
		SimpleClass(String arg0) {
			this.arg0 = arg0;
		}
		String getVal() {
			return arg0;
		}
	}
	
	@Test
	public void testOneInvocation(SimpleClass simple) {
		String value = simple.getVal();
		
		assertEquals("foo", value);
		
		verify(simple).getVal();
	}
	
	@Test
	public void testNeverInvoked(SimpleClass simple) {
		verify(simple, never()).getVal();
	}
}
