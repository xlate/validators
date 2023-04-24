package io.xlate.validation.internal.constraintvalidators;

import jakarta.el.BeanNameResolver;
import jakarta.el.ELProcessor;
import jakarta.validation.ConstraintDeclarationException;

public interface BooleanExpression {

    default boolean evaluate(ELProcessor processor, String expression) {
        if (!expression.isEmpty()) {
            Object result = processor.eval(expression);

            if (result instanceof Boolean) {
                return (Boolean) result;
            } else {
                throw new ConstraintDeclarationException("Expression `" + expression + "` does not evaluate to Boolean");
            }
        }

        return true;
    }

    default BeanNameResolver newNameResolver(Object target, String targetName) {
        return new BeanNameResolver() {
            @Override
            public boolean isNameResolved(String beanName) {
                return targetName.equals(beanName);
            }

            @Override
            public Object getBean(String beanName) {
                return isNameResolved(beanName) ? target : null;
            }
        };
    }

}
