package io.xlate.validation.constraintvalidation;

import java.util.HashMap;
import java.util.Map;

import javax.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.xlate.validation.constraints.Expression;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class ExpressionValidatorTest {

    ExpressionValidator target;

    @Mock ConstraintValidatorContext context;

    @Mock Expression annotation;

    /*@BeforeEach
    void initialize() {
        target = new ExpressionValidator();
    }*/

    @Test
    void testLiteralTrue() {
        target = new ExpressionValidator();
        Mockito.when(annotation.value()).thenReturn("true");
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(null, context));
    }

    @Test
    void testLiteralFalse() {
        target = new ExpressionValidator();
        Mockito.when(annotation.value()).thenReturn("false");
        target.initialize(annotation);
        Assertions.assertFalse(target.isValid(null, context));
    }

    @Test
    void testStringIdentityTrue() {
        target = new ExpressionValidator();
        Mockito.when(annotation.value()).thenReturn("'I' == 'I'");
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(null, context));
    }

    @Test
    void testNumericComparison() {
        target = new ExpressionValidator();
        Mockito.when(annotation.value()).thenReturn("self.smaller lt self.larger");
        Map<String, Integer> data = new HashMap<>();
        data.put("smaller", 1);
        data.put("larger", 2);
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(data, context));
    }


}
