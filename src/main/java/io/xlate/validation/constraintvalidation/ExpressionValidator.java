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
package io.xlate.validation.constraintvalidation;

import javax.el.ELProcessor;
import javax.validation.ConstraintDefinitionException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.xlate.validation.constraints.Expression;

public class ExpressionValidator implements ConstraintValidator<Expression, Object> {

    private Expression annotation;

    @Override
    public void initialize(Expression constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        String expression = annotation.value();
        ELProcessor processor = new ELProcessor();
        processor.defineBean("self", target);

        Object result = processor.eval(annotation.value());

        if (result instanceof Boolean) {
            return (Boolean) result;
        }

        throw new ConstraintDefinitionException("Expression `" + expression + "` does evaluate to Boolean");
    }
}
