package io.xlate.validation.constraintvalidation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void initialize() {
        target = new ExpressionValidator();
    }

    @Test
    void testLiteralTrue() {
        Mockito.when(annotation.value()).thenReturn("true");
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(null, context));
    }

    @Test
    void testLiteralFalse() {
        Mockito.when(annotation.value()).thenReturn("false");
        target.initialize(annotation);
        Assertions.assertFalse(target.isValid(null, context));
    }

    @Test
    void testStringIdentityTrue() {
        Mockito.when(annotation.value()).thenReturn("'I' == 'I'");
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(null, context));
    }

    @Test
    void testNumericComparison() {
        Mockito.when(annotation.value()).thenReturn("self.smaller lt self.larger");
        Map<String, Integer> data = new HashMap<>();
        data.put("smaller", 1);
        data.put("larger", 2);
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(data, context));
    }

    @Test
    void testDateComparison() {
        Mockito.when(annotation.value()).thenReturn("self.earlier lt self.later");
        Map<String, Date> data = new HashMap<>();
        data.put("earlier", new Date(1));
        data.put("later", new Date());
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(data, context));
    }
}
