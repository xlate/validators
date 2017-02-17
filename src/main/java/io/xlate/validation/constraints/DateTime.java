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

import io.xlate.validation.constraintvalidators.DateTimeValidator;

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
