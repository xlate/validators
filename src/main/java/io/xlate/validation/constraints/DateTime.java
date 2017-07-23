/*******************************************************************************
 * Copyright 2017 xlate.io, http://www.xlate.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package io.xlate.validation.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.xlate.validation.constraintvalidation.DateTimeValidator;

/**
 * The annotated element value must contain characters valid for the given
 * patterns. The value must be able to be parsed.
 * <p/>
 * Supported types are:
 * <ul>
 * <li>{@code CharSequence}</li>
 * </ul>
 * <p/>
 * {@code null} elements are considered valid.
 *
 * @author Michael Edgar
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { DateTimeValidator.class })
public @interface DateTime {

    String message();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return patterns the valid date patterns to which the annotated element must conform
     */
    String[] patterns();

    /**
     * @return lenient whether the parsing mode should be lenient
     */
    boolean lenient() default false;
}
