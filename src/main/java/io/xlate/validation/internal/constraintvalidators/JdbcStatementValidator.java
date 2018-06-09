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
package io.xlate.validation.internal.constraintvalidators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.el.ELProcessor;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;

import io.xlate.validation.constraints.JdbcStatement;

public class JdbcStatementValidator implements ConstraintValidator<JdbcStatement, Object> {

    JdbcStatement annotation;
    DataSource dataSource;

    @Override
    public void initialize(JdbcStatement constraintAnnotation) {
        this.annotation = constraintAnnotation;
        this.dataSource = getDataSource(annotation.dataSourceLookup());
    }

    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        final String sql = annotation.value();
        final String[] parameters = annotation.parameters();
        final boolean valid = executeQuery(target, sql, parameters);

        if (!valid) {
            updateValidationContext(context, annotation.node(), annotation.message());
        }

        return valid;
    }

    DataSource getDataSource(String dataSourceLookup) {
        final DataSource dataSource;

        try {
            if (dataSourceLookup.isEmpty()) {
                dataSource = InitialContext.doLookup("java:comp/DefaultDataSource");
            } else {
                dataSource = InitialContext.doLookup(dataSourceLookup);
            }
        } catch (NamingException e) {
            throw new ValidationException("DataSource not found", e);
        }

        return dataSource;
    }

    boolean executeQuery(Object target, String sql, String[] parameters) {
        final boolean valid;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setParameters(target, parameters, statement);

                try (ResultSet results = statement.executeQuery()) {
                    valid = results.next();
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(e);
        }

        return valid;
    }

    void setParameters(Object target, String[] parameters, PreparedStatement statement) {
        if (parameters.length == 0) {
            return;
        }

        ELProcessor processor = new ELProcessor();
        processor.defineBean("self", target);
        int p = 0;

        for (String parameterExpression : parameters) {
            Object parameter;

            try {
                parameter = processor.eval(parameterExpression);
            } catch (Exception e) {
                throw new ConstraintDeclarationException(e);
            }

            try {
                statement.setObject(++p, parameter);
            } catch (SQLException e) {
                String msg = "Expression `" + parameterExpression +
                        "` does not evaluate to a valid JDBC parameter for marker #" + p;
                throw new ConstraintDeclarationException(msg, e);
            }
        }
    }

    void updateValidationContext(ConstraintValidatorContext context, String nodeName, String message) {
        if (nodeName.isEmpty()) {
            return;
        }

        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(message)
               .addPropertyNode(nodeName)
               .addConstraintViolation();
    }
}
