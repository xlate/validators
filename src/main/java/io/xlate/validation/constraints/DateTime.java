/*******************************************************************************
 * Copyright (C) 2018 xlate.io LLC, http://www.xlate.io
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
