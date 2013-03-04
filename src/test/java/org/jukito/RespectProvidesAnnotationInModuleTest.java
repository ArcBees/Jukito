package org.jukito;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

/**
 * @author Przemysław Gałązka
 * @since 08-02-2013
 */
@RunWith(JukitoRunner.class)
public class RespectProvidesAnnotationInModuleTest {


  @Test
  public void shouldRespectProvidesAnnotationUsedInModule(SomeTestClass someTestClass) throws Exception {
    //-------------------- GIVEN -------------------------------------------------------------------

    //-------------------- WHEN --------------------------------------------------------------------

    //-------------------- THEN --------------------------------------------------------------------
    // injected object should be created by factory method
    // defined in  custom module  ModuleWithProvidesMethods.
    // Init method should be called from factory method
    verify(someTestClass).someInitMethod();
  }


  public static class A extends JukitoModule {

    @Override
    protected void configureTest() {
      install(new ModuleWithProvidesMethods());
    }
  }

}
