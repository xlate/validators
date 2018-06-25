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
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;

import io.xlate.validation.internal.constraintvalidators.ExpressionValidator;

/**
 * Constraint to evaluate an arbitrary EL expression with the annotated target
 * available as 'self' in expressions. The EL expression must evaluate to
 * Boolean.TRUE in order for the annotated target to be valid.
 *
 * @author Michael Edgar
 */
@Target({ TYPE, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ExpressionValidator.class })
@Repeatable(Expression.List.class)
public @interface Expression {

    String message() default "expression `{value}` does not evaluate to true";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return the expression to evaluate to determine the validity of the
     *         constraint
     */
    String value();

    /**
     * An EL expression used to determine if the expression given by
     * {@link #value()} should be checked. This expression is available to
     * short-circuit the constraint validation of this {@link Expression} in
     * scenarios when it should not apply, e.g. a value is null and the
     * constraint only applies to non-null values.
     *
     * @return the expression to evaluate to determine whether the constraint
     *         should be checked
     */
    String when() default "";

    /**
     * Name of the node to be identified in a {@link ConstraintViolation} should
     * validation fail.
     *
     * @return the name of the node to be identified in a
     *         {@link ConstraintViolation} should validation fail.
     */
    String node() default "";

    /**
     * Defines several {@link Expression} annotations on the same element.
     *
     * @see io.xlate.validation.constraints.Expression
     */
    @Target({ TYPE, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Expression[] value();
    }
}
