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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used on one or more parameter of a test function.
 * The test function will be executed multiple times, one for each value bound
 * to the parameter type.
 * <p/>
 * If more than one parameter is annotated with {@literal @}{@link All} then
 * all combinations will be used. Therefore, be careful when using it on more than
 * two or three parameters as it can result in a combinatorial explosion.
 * <p/>
 * Using the additional parameter {@link #value()} a subset of all bound values
 * can be specified to be run in the test function.
 *
 * @see {@link TestModule#bindMany}
 * @see {@link TestModule#bindManyInstances}
 * @see {@link TestModule#bindManyNamed}
 * @see {@link TestModule#bindManyNamedInstances}
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface All {
    String DEFAULT = "__ALL__";

    /**
     * Used in conjunction with {@link org.jukito.JukitoModule#bindManyNamed(Class, String, Class[])} and related
     * methods to retrieve all objects binded with the given name.
     */
    String value() default DEFAULT;
}
