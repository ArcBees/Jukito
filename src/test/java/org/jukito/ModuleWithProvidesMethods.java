package org.jukito;

import com.google.inject.Provides;

import static org.mockito.Mockito.*;

/**
 * @author Przemysław Gałązka
 * @since 08-02-2013
 */
public class ModuleWithProvidesMethods extends JukitoModule {


  public ModuleWithProvidesMethods() {
    // Workaround for http://code.google.com/p/jukito/issues/detail?id=40
    setTestClass(RespectProvidesAnnotationInModuleTest.class);
  }


  @Override
  protected void configureTest() {
  }


  @Provides
  SomeTestClass create() {
    SomeTestClass mock = mock(SomeTestClass.class);
    mock.someInitMethod();
    return mock;
  }


  @Override
  protected void bindScopes() {
    // Workaround for http://code.google.com/p/jukito/issues/detail?id=40
  }
}
