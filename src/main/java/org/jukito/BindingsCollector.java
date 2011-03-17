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
import java.util.ArrayList;
import java.util.List;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.Message;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.UntargettedBinding;
/**
 * Collects all the bindings from a Guice module, so that Jukito can identify missing
 * bindings and bind them to mock or instances.
 */
public class BindingsCollector {
  private final AbstractModule module;
  private final List<BindingInfo> bindingsObserved = new ArrayList<BindingInfo>();
  BindingsCollector(AbstractModule module) {
    this.module = module;
  }
  public void collectBindings() {
    GuiceElementVisitor visitor = new GuiceElementVisitor();
    visitor.visitElements(Elements.getElements(module));
    // TODO report errors?
  }
  public List<BindingInfo> getBindingsObserved() {
    return bindingsObserved;
  }
  /**
   * Information on a binding, used by Jukito to identify provided keys and needed keys.
   */
  public static class BindingInfo {
    Key<?> key;
    Key<?> boundKey;
    public Object boundInstance;
  }
  private final List<Message> messages = new ArrayList<Message>();
  /**
   * This visitor collects all information on various guice elements.
   */
  public class GuiceElementVisitor extends DefaultElementVisitor<Void> {
    private void visitElements(List<Element> elements) {
      for (Element element : elements) {
        element.acceptVisitor(this);
      }
    }
    @Override
    public <T> Void visit(com.google.inject.Binding<T> command) {
      GuiceBindingVisitor<T> bindingVisitor = new GuiceBindingVisitor<T>();
      command.acceptTargetVisitor(bindingVisitor);
      return null;
    }
    @Override
    public Void visit(Message message) {
      messages.add(message);
      return null;
    }
  }
  /**
   * This visitor collects all information on guice bindings.
   */
  public class GuiceBindingVisitor<T> extends DefaultBindingTargetVisitor<T, Void> {
    @Override
    public Void visit(ProviderBinding<? extends T> providerBinding) {
      BindingInfo binding = new BindingInfo();
      binding.key = providerBinding.getKey();
      binding.boundKey = providerBinding.getProvidedKey();
      bindingsObserved.add(binding);
      return null;
    }
    @Override
    public Void visit(ProviderKeyBinding<? extends T> providerKeyBinding) {
      BindingInfo binding = new BindingInfo();
      binding.key = providerKeyBinding.getKey();
      binding.boundKey = providerKeyBinding.getProviderKey();
      bindingsObserved.add(binding);
      return null;
    }
    @Override
    public Void visit(ProviderInstanceBinding<? extends T> providerInstanceBinding) {
      BindingInfo binding = new BindingInfo();
      binding.key = providerInstanceBinding.getKey();
      binding.boundInstance = providerInstanceBinding.getProviderInstance();
      bindingsObserved.add(binding);
      return null;
    }
    @Override
    public Void visit(InstanceBinding<? extends T> instanceBinding) {
      BindingInfo binding = new BindingInfo();
      binding.key = instanceBinding.getKey();
      binding.boundInstance = instanceBinding.getInstance();
      bindingsObserved.add(binding);
      return null;
    }
    @Override
    public Void visit(ConvertedConstantBinding<? extends T> constantBinding) {
      BindingInfo binding = new BindingInfo();
      binding.key = constantBinding.getKey();
      binding.boundInstance = constantBinding.getValue();
      bindingsObserved.add(binding);
      return null;
    }
    @Override
    public Void visit(UntargettedBinding<? extends T> untargettedBinding) {
      BindingInfo binding = new BindingInfo();
      binding.key = untargettedBinding.getKey();
      binding.boundKey = untargettedBinding.getKey();
      bindingsObserved.add(binding);
      return null;
    }
    @Override
    public Void visit(LinkedKeyBinding<? extends T> linkedKeyBinding) {
      BindingInfo binding = new BindingInfo();
      binding.key = linkedKeyBinding.getKey();
      binding.boundKey = linkedKeyBinding.getLinkedKey();
      bindingsObserved.add(binding);
      return null;
    }
  }
}