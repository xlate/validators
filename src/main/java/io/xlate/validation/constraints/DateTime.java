/*******************************************************************************
 * Copyright (C) 2018 xlate.io LLC, http://www.xlate.io
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.format.ResolverStyle;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import io.xlate.validation.internal.constraintvalidators.DateTimeValidator;

/**
 * The annotated element value must contain characters valid for the given
 * patterns. The value must be able to be parsed.
 * <p>
 * Supported types are:
 * <ul>
 * <li>{@code CharSequence}</li>
 * </ul>
 * <p>
 * {@code null} elements are considered valid.
 *
 * @author Michael Edgar
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { DateTimeValidator.class })
@Repeatable(DateTime.List.class)
public @interface DateTime {

    public enum ParserType {
        /**
         * Utilize {@link java.text.SimpleDateFormat} to parse date strings
         */
        JAVA_TEXT,
        /**
         * Utilize {@link java.time.format.DateTimeFormatter} to parse date
         * strings
         */
        JAVA_TIME
    }

    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Valid date patterns to which the annotated element must conform. Legal
     * values for the patterns depends on the value of {@link #parserType()}
     * being used.
     *
     * @return patterns the valid date patterns to which the annotated element
     *         must conform
     */
    String[] patterns();

    /**
     * @return lenient whether the parsing mode should be lenient. Ignored
     *         unless {@link #parserType()} is {@link ParserType#JAVA_TEXT}.
     */
    boolean lenient() default false;

    /**
     * @return formatter type to use for parsing date/time strings during
     *         validation
     * @since 1.2
     */
    ParserType parserType() default ParserType.JAVA_TEXT;

    /**
     * @return the resolver style to use during parsing. Ignored unless
     *         {@link #parserType()} is {@link ParserType#JAVA_TIME}.
     * @see java.time.format.DateTimeFormatter#getResolverStyle
     * @since 1.2
     */
    ResolverStyle resolverStyle() default ResolverStyle.SMART;

    /**
     * Defines several {@link DateTime} annotations on the same element.
     *
     * @see io.xlate.validation.constraints.DateTime
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        DateTime[] value();
    }
}
