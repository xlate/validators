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
import javax.validation.constraints.NotNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.xlate.validation.constraints.Expression;

@ExtendWith(MockitoExtension.class)
class ExpressionValidatorIT {

    Validator validator;

    @Expression(value = "self.value1 ne self.value2", node = "value2")
    @Expression(value = "self.value2 ne 'illegal-value'", message = "value2 must be legal")
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

    @Expression(
        targetName = "target",
        value = "target.newPassword eq target.newPasswordConfirmation",
        node = "newPasswordConfirmation",
        message = "Password confirmation must match new password")
    public static class PasswordBean {
        @NotNull
        private String newPassword;
        @NotNull
        private String newPasswordConfirmation;

        public String getNewPassword() {
            return newPassword;
        }

        public String getNewPasswordConfirmation() {
            return newPasswordConfirmation;
        }
    }

    public static class ImportBean {
        public static final String TEST = "TEST";

        @Expression(
            value = "TEST.equals(self)",
            staticImports = "io.xlate.validation.internal.constraintvalidators.ExpressionValidatorIT$ImportBean.TEST")
        @Expression(
            value = "ExpressionValidatorIT$ImportBean.TEST.equals(self)",
            classImports = "io.xlate.validation.internal.constraintvalidators.ExpressionValidatorIT$ImportBean")
        @Expression(
            value = "ExpressionValidatorIT$ImportBean.TEST.equals(self)",
            packageImports = "io.xlate.validation.internal.constraintvalidators")
        private String value1;

        public String getValue1() {
            return value1;
        }
    }

    @BeforeEach
    void initialize() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValue1EqualsIllegalValue() {
        TestBean bean = new TestBean();
        bean.value1 = "illegal-value";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("value1", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testValue1EqualsLegalValue() {
        TestBean bean = new TestBean();
        bean.value1 = "legal-value";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertEquals(0, violations.size());
    }

    @Test
    void testBeanValuesEqualInvalid() {
        TestBean bean = new TestBean();
        bean.value1 = "legal-value";
        bean.value2 = "legal-value";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("value2", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testValue2InvalidFromBeanLevelWithoutNodeSpecified() {
        TestBean bean = new TestBean();
        bean.value2 = "illegal-value";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("", violations.iterator().next().getPropertyPath().toString());
        Assertions.assertEquals("value2 must be legal", violations.iterator().next().getMessage());
    }

    @Test
    void testPasswordValuesMatch() {
        PasswordBean bean = new PasswordBean();
        bean.newPassword = "newvalue";
        bean.newPasswordConfirmation = "newvalue";
        Set<ConstraintViolation<PasswordBean>> violations = validator.validate(bean);
        Assertions.assertEquals(0, violations.size());
    }

    @Test
    void testPasswordValuesNotMatch() {
        PasswordBean bean = new PasswordBean();
        bean.newPassword = "newvalue";
        bean.newPasswordConfirmation = "newvalue1";
        Set<ConstraintViolation<PasswordBean>> violations = validator.validate(bean);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("newPasswordConfirmation", violations.iterator().next().getPropertyPath().toString());
        Assertions.assertEquals("Password confirmation must match new password",
                                violations.iterator().next().getMessage());
    }

    @Test
    void testValueEqualsStaticValue() {
        ImportBean bean = new ImportBean();
        bean.value1 = ImportBean.TEST;
        Set<ConstraintViolation<ImportBean>> violations = validator.validate(bean);
        Assertions.assertEquals(0, violations.size());
    }
}
