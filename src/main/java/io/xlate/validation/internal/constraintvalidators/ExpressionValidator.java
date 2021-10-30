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

import java.util.Arrays;

import javax.el.BeanNameResolver;
import javax.el.ELManager;
import javax.el.ELProcessor;
import javax.el.ImportHandler;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.xlate.validation.constraints.Expression;

public class ExpressionValidator implements BooleanExpression, ConstraintValidator<Expression, Object> {

    private Expression annotation;

    @Override
    public void initialize(Expression constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        ELProcessor processor = buildProcessor(target);

        if (!evaluate(processor, annotation.when())) {
            return true;
        }

        if (!evaluate(processor, annotation.value())) {
            String nodeName = annotation.node();

            if (!nodeName.isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(annotation.message())
                       .addPropertyNode(nodeName)
                       .addConstraintViolation();
            }

            return false;
        }

        return true;
    }

    ELProcessor buildProcessor(Object target) {
        ELProcessor processor = new ELProcessor();
        ELManager manager = processor.getELManager();
        ImportHandler imports = manager.getELContext().getImportHandler();
        String targetName = annotation.targetName();

        manager.addBeanNameResolver(new BeanNameResolver() {
            @Override
            public boolean isNameResolved(String beanName) {
                return targetName.equals(beanName);
            }

            @Override
            public Object getBean(String beanName) {
                return isNameResolved(beanName) ? target : null;
            }
        });

        Arrays.stream(annotation.packageImports()).forEach(imports::importPackage);
        Arrays.stream(annotation.classImports()).forEach(imports::importClass);
        Arrays.stream(annotation.staticImports()).forEach(imports::importStatic);

        return processor;
    }
}
