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
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import io.xlate.validation.internal.constraintvalidators.JdbcStatementValidator;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;

/**
 * Constraint to execute a read-only JDBC statement (i.e., a query). By default,
 * if the query returns at least one result row, the validation is considered to
 * be successful.
 *
 * Statements are executed using the default {@link DataSource},
 * <code>java:comp/DefaultDataSource</code>.
 *
 * @author Michael Edgar
 */
@Target({ TYPE, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { JdbcStatementValidator.class })
@Repeatable(JdbcStatement.List.class)
public @interface JdbcStatement {

    String message() default "statement `{value}` did not return any results";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * The JDBC statement to execute to determine the validity of the
     * constraint. The statement must be a valid SQL text that may be used with
     * {@link Connection#prepareStatement(String)} and (subsequently)
     * {@link PreparedStatement#executeQuery()}. The validation is considered
     * successful when the result of the statement execution returns at least
     * one row from the database.
     *
     * @return the JDBC query to evaluate to determine the validity of the
     *         constraint
     */
    String value();

    /**
     * The list of EL expressions used to derive parameters values to be bound
     * to parameter markers (i.e. ?) in the SQL statement given by
     * {@link #value()}. The annotated target is available as 'self' in
     * expressions. The parameters must be given in the same order that they are
     * to be bound to the parameter marker in the SQL statement.
     *
     * @return the list of EL expressions used to derive parameters values
     */
    String[] parameters() default {};

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
     * The JNDI lookup name of the resource to be used to obtain a
     * {@link Connection}. It can link to any compatible {@link DataSource}
     * using the global JNDI names.
     *
     * @return the JNDI lookup name of the resource to be used to obtain a
     *         {@link Connection}
     */
    String dataSourceLookup() default "";

    /**
     * Name of the node to be identified in a {@link ConstraintViolation} should
     * validation fail.
     *
     * @return the name of the node to be identified in a
     *         {@link ConstraintViolation} should validation fail.
     */
    String node() default "";

    /**
     * Defines several {@link JdbcStatement} annotations on the same element.
     *
     * @see io.xlate.validation.constraints.JdbcStatement
     */
    @Target({ TYPE, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        JdbcStatement[] value();
    }
}
