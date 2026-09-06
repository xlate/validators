package io.xlate.validation.internal.constraintvalidators;

import java.util.Arrays;
import java.util.function.Predicate;

import jakarta.el.ELManager;
import jakarta.el.ELProcessor;
import jakarta.el.ImportHandler;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

import io.xlate.validation.constraints.Expression;

@SupportedValidationTarget({
    ValidationTarget.ANNOTATED_ELEMENT,
    ValidationTarget.PARAMETERS
})
public class ExpressionValidator implements BooleanExpression, ConstraintValidator<Expression, Object> {

    private Expression annotation;

    @Override
    public void initialize(Expression constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        ELProcessor processor = buildProcessor(target);

        if (!evaluate(processor, annotation.when(), null)) {
            return true;
        }

        if (!evaluate(processor, annotation.value(), annotation.exceptionalValue().booleanValue())) {
            String[] node = Arrays.stream(annotation.node())
                    .filter(Predicate.not(String::isEmpty))
                    .toArray(String[]::new);

            if (node.length > 0) {
                context.disableDefaultConstraintViolation();
                NodeBuilderCustomizableContext builder = context
                        .buildConstraintViolationWithTemplate(annotation.message())
                        .addPropertyNode(node[0]);

                for (int n = 1; n < node.length; n++) {
                    builder = builder.addPropertyNode(node[n]);
                }

                builder.addConstraintViolation();
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

        manager.addBeanNameResolver(newNameResolver(target, targetName));

        Arrays.stream(annotation.packageImports()).forEach(imports::importPackage);
        Arrays.stream(annotation.classImports()).forEach(imports::importClass);
        Arrays.stream(annotation.staticImports()).forEach(imports::importStatic);

        return processor;
    }
}
