/**
 * Copyright 2011 ArcBees Inc.
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

    public static String getName(Annotation annotation) {
        if (annotation instanceof Internal) {
            return ((Internal) annotation).name();
        }
        return All.DEFAULT;
    }

    /**
     * Returns an annotation instance that is not equal to any other annotation
     * instances, for use in creating distinct {@link com.google.inject.Key}s.
     */
    public static Annotation create(String name) {
        return create(nextUniqueValue.getAndIncrement(), name);
    }

    private static Annotation create(final int value, final String name) {
        return new Internal() {
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
                    if (other.value() != value()) {
                        return false;
                    }
                    if (name() == null) {
                        return other.name() == null;
                    }
                    return name().equals(other.name());
                }
                return false;
            }

            @Override
            public int hashCode() {
                if (name == null) {
                    return (127 * "value".hashCode()) ^ value;
                }
                return (127 * name.hashCode()) ^ value;
            }
        };
    }

    @Retention(RUNTIME)
    @BindingAnnotation
    private @interface Internal {
        int value();
        String name();
    }
}