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

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Factory for unique annotations with a name. Based on {@link com.google.inject.internal.UniqueAnnotations}.
 */
class NamedUniqueAnnotations {
    private static final AtomicInteger nextUniqueValue = new AtomicInteger(1);

    /**
     * Returns if a NamedUniqueAnnotations matches a binding name
     *
     * @param bindingName the name to match.
     * @param annotation the annotation to match the name to.
     * @return if the annotation matches the bindingName.
     */
    public static boolean matches(String bindingName, java.lang.annotation.Annotation annotation) {
        if (annotation instanceof Internal) {
            return ((Internal) annotation).name().equals(bindingName);
        }
        return false;
    }

    /**
     * Returns an annotation instance that is not equal to any other annotation
     * instances, for use in creating distinct {@link com.google.inject.Key}s.
     *
     * @param name name to group multiple annotations. Each annotation is still unique even if it belongs to a group.
     */
    public static Annotation create(String name) {
        int unique = nextUniqueValue.getAndIncrement();
        String nonNullName = name == null ? All.DEFAULT : name;
        return new InternalImpl(unique, nonNullName);
    }

    @Retention(RUNTIME)
    @BindingAnnotation
    private @interface Internal {
        String name();
        int value();
    }

    private static class InternalImpl implements Internal {
        private final int value;
        private final String name;

        public InternalImpl(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int value() {
            return value;
        }

        public String name() {
            return name;
        }

        public Class<? extends Annotation> annotationType() {
            return Internal.class;
        }

        @Override
        public String toString() {
            return "@" + Internal.class.getName() + "(name=" + name + ", value=" + value + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof Internal) {
                Internal other = (Internal) o;
                return value() == other.value() && name().equals(other.name());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (127 * name.hashCode()) ^ value;
        }
    }
}
