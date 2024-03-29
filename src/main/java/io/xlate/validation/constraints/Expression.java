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

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;

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
     * When the constraint is placed on a method target, this determines whether
     * the validation target is the method's return value or its parameters.
     *
     * @since 1.4
     */
    ConstraintTarget validationAppliesTo() default ConstraintTarget.IMPLICIT;

    /**
     * A boolean expression evaluated to determine whether the constraint
     * is valid. When the expression is true, it is considered valid. Else,
     * it is considered invalid.
     *
     * @return the expression to evaluate to determine the validity of the
     *         constraint
     */
    String value();

    /**
     * A boolean EL expression used to determine if the expression given by
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
     * Name of the nodes to be identified in a {@link ConstraintViolation} should
     * validation fail. If more than one entry is present, each will be added to
     * the violation as a separate property node.
     *
     * @return the name of the nodes to be identified in a
     *         {@link ConstraintViolation} should validation fail.
     */
    String[] node() default {};

    /**
     * The name to be used within the {@link #value()} and {@link #when()}
     * expressions to refer to the target of the {@link Expression @Expression}
     * annotation.
     *
     * @return the name to use for binding the annotation target within
     *         expressions
     */
    String targetName() default "self";

    /**
     * The packages to be imported to the EL context for evaluating the
     * expressions in {@link #value()} and {@link #when()}. All classes in the
     * package will be imported.
     *
     * @return the packages to be imported to the EL context during expression
     *         evaluation
     *
     * @see jakarta.el.ImportHandler#importPackage(String)
     */
    String[] packageImports() default {};

    /**
     * The classes to be imported to the EL context for evaluating the
     * expressions in {@link #value()} and {@link #when()}.
     *
     * @return the classes to be imported to the EL context during expression
     *         evaluation
     *
     * @see jakarta.el.ImportHandler#importClass(String)
     */
    String[] classImports() default {};

    /**
     * The static member name, including the full class name, to be imported to
     * the EL context for evaluating the expressions in {@link #value()} and
     * {@link #when()}.
     *
     * @return the static member names to be imported to the EL context during
     *         expression evaluation
     *
     * @see jakarta.el.ImportHandler#importStatic(String)
     */
    String[] staticImports() default {};

    /**
     * Value used in place of the {@link #value()} expression's return when
     * evaluation results in an exception being thrown.
     *
     * @return value used in place of the result of the {@link #value()}
     *         expression
     * @since 1.3
     */
    ExceptionalValue exceptionalValue() default ExceptionalValue.UNSET;

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

    /**
     * Values allowed for {@linkplain Expression#exceptionalValue()}.
     *
     * @since 1.3
     */
    enum ExceptionalValue {
        TRUE(Boolean.TRUE),
        FALSE(Boolean.FALSE),
        UNSET(null);

        final Boolean booleanValue;

        private ExceptionalValue(Boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public Boolean booleanValue() {
            return booleanValue;
        }
    }

}
