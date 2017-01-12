/**
 * Copyright 2013 ArcBees Inc.
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.DefaultBindingScopingVisitor;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.Message;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.StaticInjectionRequest;
import com.google.inject.spi.UntargettedBinding;

/**
 * Collects all the bindings from a Guice module, so that Jukito can identify missing
 * bindings and bind them to mock or instances.
 */
public class BindingsCollector {

    /**
     * Information on a binding, used by Jukito to identify provided keys and needed keys.
     */
    public static class BindingInfo {

        public Object boundInstance;
        Key<?> key;
        Key<?> boundKey;
        String scope;

        public static BindingInfo create(Binding<?> binding, Key<?> boundKey,
                Object instance) {
            BindingInfo bindingInfo = new BindingInfo();
            bindingInfo.key = binding.getKey();
            bindingInfo.boundKey = boundKey;
            bindingInfo.boundInstance = instance;
            bindingInfo.scope = binding.acceptScopingVisitor(new GuiceScopingVisitor());
            return bindingInfo;
        }

        public static BindingInfo create(Key<?> boundKey) {
            BindingInfo bindingInfo = new BindingInfo();
            bindingInfo.boundKey = boundKey;

            return bindingInfo;
        }
    }

    private final AbstractModule module;
    private final List<BindingInfo> bindingsObserved = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();

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
            GuiceBindingVisitor<T> bindingVisitor = new GuiceBindingVisitor<>();
            command.acceptTargetVisitor(bindingVisitor);
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Void visit(PrivateElements privateElements) {
            Set<Key<?>> exposedKeys = privateElements.getExposedKeys();
            for (Element element : privateElements.getElements()) {
                if (element instanceof Binding<?>) {
                    Binding<?> bindingElement = (Binding<?>) element;
                    if (exposedKeys.contains(bindingElement.getKey())) {
                        @SuppressWarnings("rawtypes")
                        GuicePrivateBindingVisitor bindingVisitor = new GuicePrivateBindingVisitor();
                        bindingElement.acceptTargetVisitor(bindingVisitor);
                    }
                }
            }
            return null;
        }

        @Override
        public Void visit(StaticInjectionRequest staticInjectionRequest) {
            for (InjectionPoint injectionPoint : staticInjectionRequest.getInjectionPoints()) {
                addInjectionPointDependencies(injectionPoint);
            }

            return super.visit(staticInjectionRequest);
        }

        @Override
        public Void visit(Message message) {
            messages.add(message);
            return null;
        }

        private void addInjectionPointDependencies(InjectionPoint injectionPoint) {
            // Do not consider dependencies coming from optional injections.
            if (injectionPoint.isOptional()) {
                return;
            }

            for (Dependency<?> dependency : injectionPoint.getDependencies()) {
                Key<?> key = dependency.getKey();

                bindingsObserved.add(BindingInfo.create(key));
            }
        }
    }

    /**
     * This visitor collects all information on guice bindings.
     */
    public class GuiceBindingVisitor<T> extends DefaultBindingTargetVisitor<T, Void> {

        protected Void addBindingInfo(Binding<? extends T> binding, Key<?> boundKey, Object instance) {
            bindingsObserved.add(BindingInfo.create(binding, boundKey, instance));
            return null;
        }

        private Void addBinding(Binding<? extends T> binding) {
            return addBindingInfo(binding, binding.getKey(), null);
        }

        private Void addBindingKey(Binding<? extends T> binding, Key<?> boundKey) {
            return addBindingInfo(binding, boundKey, null);
        }

        private Void addBindingInstance(Binding<? extends T> binding, Object instance) {
            return addBindingInfo(binding, null, instance);
        }

        @Override
        public Void visit(ProviderBinding<? extends T> providerBinding) {
            return addBindingKey(providerBinding, providerBinding.getProvidedKey());
        }

        @Override
        public Void visit(ProviderKeyBinding<? extends T> providerKeyBinding) {
            return addBindingKey(providerKeyBinding, providerKeyBinding.getProviderKey());
        }

        @Override
        public Void visit(ProviderInstanceBinding<? extends T> providerInstanceBinding) {
            return addBindingInstance(providerInstanceBinding,
                    providerInstanceBinding.getProviderInstance());
        }

        @Override
        public Void visit(InstanceBinding<? extends T> instanceBinding) {
            return addBindingInstance(instanceBinding, instanceBinding.getInstance());
        }

        @Override
        public Void visit(ConvertedConstantBinding<? extends T> constantBinding) {
            return addBindingInstance(constantBinding, constantBinding.getValue());
        }

        @Override
        public Void visit(UntargettedBinding<? extends T> untargettedBinding) {
            return addBinding(untargettedBinding);
        }

        @Override
        public Void visit(LinkedKeyBinding<? extends T> linkedKeyBinding) {
            return addBindingKey(linkedKeyBinding, linkedKeyBinding.getLinkedKey());
        }

        @Override
        public Void visit(ConstructorBinding<? extends T> constructorBinding) {
            return addBinding(constructorBinding);
        }
    }

    /**
     * This visitor collects the bindings for PrivateModules. Because the child
     * elements are private, the bound keys are not recorded.
     */
    public class GuicePrivateBindingVisitor<T> extends GuiceBindingVisitor<T> {

        @Override
        public Void visit(LinkedKeyBinding<? extends T> linkedKeyBinding) {
            return addBindingInfo(linkedKeyBinding, null, null);
        }
    }

    /**
     * This visitor collects all information on guice scopes associated to the bindings.
     */
    public static class GuiceScopingVisitor extends DefaultBindingScopingVisitor<String> {

        @Override
        public String visitEagerSingleton() {
            return "EagerSingleton";
        }

        @Override
        public String visitScope(Scope scope) {
            return scope.toString();
        }

        @Override
        public String visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
            return scopeAnnotation.getCanonicalName();
        }
    }
}
