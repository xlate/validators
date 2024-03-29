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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jakarta.el.PropertyNotFoundException;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.xlate.validation.constraints.Expression;
import io.xlate.validation.constraints.Expression.ExceptionalValue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExpressionValidatorTest {

    static final String[] EMPTY_STRING_ARRAY = { };
    ExpressionValidator target;

    @Mock
    ConstraintValidatorContext context;

    @Mock
    Expression annotation;

    @BeforeEach
    void setUp() {
        target = new ExpressionValidator();
        Mockito.when(annotation.when()).thenReturn("");
        Mockito.when(annotation.node()).thenReturn(new String[0]);
        Mockito.when(annotation.targetName()).thenReturn("self");
        Mockito.when(annotation.packageImports()).thenReturn(EMPTY_STRING_ARRAY);
        Mockito.when(annotation.classImports()).thenReturn(EMPTY_STRING_ARRAY);
        Mockito.when(annotation.staticImports()).thenReturn(EMPTY_STRING_ARRAY);
        Mockito.when(annotation.exceptionalValue()).thenReturn(ExceptionalValue.UNSET);
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

    @Test
    void testDivision() {
        Mockito.when(annotation.value()).thenReturn("3 div 4 eq 0.75");
        Map<String, Date> data = new HashMap<>();
        data.put("earlier", new Date(1));
        data.put("later", new Date());
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(data, context));
    }

    @Test
    void testNonBoolean() {
        Mockito.when(annotation.value()).thenReturn("'a string, not a Boolean'");
        Map<String, Date> data = new HashMap<>();
        target.initialize(annotation);
        assertThrows(ConstraintDeclarationException.class, () -> {
            target.isValid(data, context);
        });
    }

    @Test
    void testPrimitiveIntArray() {
        Mockito.when(annotation.value()).thenReturn("self[0] lt self[1]");
        int[] data = { 0, 1 };
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(data, context));
    }

    @Test
    void testDateComparisonWhenConditionTrue() {
        Mockito.when(annotation.value()).thenReturn("self.earlier lt self.later");
        Mockito.when(annotation.when()).thenReturn("self.earlier.time eq 5");
        Map<String, Date> data = new HashMap<>();
        data.put("earlier", new Date(5));
        data.put("later", new Date());
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(data, context));
    }

    @Test
    void testDateComparisonWhenConditionFalse() {
        Mockito.when(annotation.value()).thenReturn("self.earlier lt self.later");
        Mockito.when(annotation.when()).thenReturn("self.earlier.time ne 1");
        Map<String, Date> data = new HashMap<>();
        data.put("earlier", new Date(1));
        data.put("later", new Date());
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(data, context));
    }

    @Test
    void testNonBooleanInWhenCondition() {
        Mockito.when(annotation.value()).thenReturn("self.earlier lt self.later");
        Mockito.when(annotation.when()).thenReturn("'0'");
        Map<String, Date> data = new HashMap<>();
        data.put("earlier", new Date(1));
        data.put("later", new Date());
        target.initialize(annotation);
        ConstraintDeclarationException ex = assertThrows(ConstraintDeclarationException.class, () -> {
            target.isValid(data, context);
        });
        Assertions.assertTrue(ex.getMessage().contains("`'0'` does not evaluate to Boolean"));
    }

    @Test
    void testTargetIsNullThenValidationPasses() {
        Mockito.when(annotation.when()).thenReturn("self == null");
        Mockito.when(annotation.value()).thenReturn("self == null");
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(null, context));
    }

    @Test
    void testTargetIsNullThenValidationFails() {
        Mockito.when(annotation.when()).thenReturn("self == null");
        Mockito.when(annotation.value()).thenReturn("self != null && self.length > 0");
        target.initialize(annotation);
        Assertions.assertFalse(target.isValid(null, context));
    }

    @Test
    void testReferenceIsUnresolveableThrowsPropertyNotFoundException() {
        Mockito.when(annotation.when()).thenReturn("invalidTarget == null");
        target.initialize(annotation);
        assertThrows(PropertyNotFoundException.class, () -> target.isValid(null, context));
    }

    @ParameterizedTest
    @CsvSource({
        "TRUE, true",
        "FALSE, false"
    })
    void testExceptionalValueUsedWhenExceptionThrown(ExceptionalValue ev, boolean expectedResult) {
        Mockito.when(annotation.value()).thenReturn("Integer.parseInt('xyz') == 0");
        Mockito.when(annotation.exceptionalValue()).thenReturn(ev);
        target.initialize(annotation);
        boolean actualResult = target.isValid(null, context);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testArgArrayElementsAccessible() {
        Mockito.when(annotation.targetName()).thenReturn("args");
        Mockito.when(annotation.when()).thenReturn("args[0] == null");
        Mockito.when(annotation.value()).thenReturn("args[1] == args[2]");
        target.initialize(annotation);
        Assertions.assertTrue(target.isValid(new Object[] { null, 1, 1 }, context));
    }

}
