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
