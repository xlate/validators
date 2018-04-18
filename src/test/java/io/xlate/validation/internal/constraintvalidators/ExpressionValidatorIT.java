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

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.xlate.validation.constraints.Expression;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class ExpressionValidatorIT {

    Validator validator;

    TestBean bean;

    @Expression(value = "self.value1 ne self.value2", node = "value2")
    public static class TestBean {
        @Expression("self ne 'illegal-value'")
        private String value1;

        private String value2;

        public String getValue1() {
            return value1;
        }

        public String getValue2() {
            return value2;
        }
    }

    @BeforeEach
    void initialize() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        bean = new TestBean();
    }

    @Test
    void testValue1EqualsIllegalValue() {
        bean.value1 = "illegal-value";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertTrue(violations.size() == 1);
        Assertions.assertEquals("value1", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testValue1EqualsLegalValue() {
        bean.value1 = "legal-value";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertTrue(violations.size() == 0);
    }

    @Test
    void testBeanValuesEqualInvalid() {
        bean.value1 = "legal-value";
        bean.value2 = "legal-value";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertTrue(violations.size() == 1);
        Assertions.assertEquals("value2", violations.iterator().next().getPropertyPath().toString());
    }
}
