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
package io.xlate.validation.constraintvalidation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.ConstraintDefinitionException;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.xlate.validation.constraints.Expression;
import io.xlate.validation.internal.constraintvalidators.ExpressionValidator;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExpressionValidatorTest {

    ExpressionValidator target;

    @Mock
    ConstraintValidatorContext context;

    @Mock
    Expression annotation;

    @BeforeEach
    void initialize() {
        target = new ExpressionValidator();
        Mockito.when(annotation.node()).thenReturn("");
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
        assertThrows(ConstraintDefinitionException.class, () -> {
            target.isValid(data, context);
        }, "a message");
    }
}
