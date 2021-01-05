package io.xlate.validation.internal.constraintvalidators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.xlate.validation.constraints.DateTime;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DateTimeValidatorIT {

    Validator validator;

    TestBean bean;

    public static class TestBean {
        @DateTime(patterns = { "yyyy-MM-dd" }, message = "Invalid ISO date")
        String isoDate;

        @DateTime(patterns = "yyyy-MM-dd", lenient = true, message = "Invalid ISO date")
        String isoDateLenient;

        @DateTime(patterns = "MM/dd/yyyy", message = "Invalid USA date")
        String usaDate;

        public Date getIsoDate() {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(isoDate);
            } catch (@SuppressWarnings("unused") ParseException e) {
                // Exception not possible after validation performed
            }
            return null;
        }
    }

    @BeforeEach
    void initialize() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        bean = new TestBean();
    }

    @Test
    void testISODateInvalidValue() {
        bean.isoDate = "2018-02-29";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertTrue(violations.size() == 1);
        Assertions.assertEquals("isoDate", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testISODateValidWhenLenient() {
        bean.isoDateLenient = "2018-02-29";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertTrue(violations.size() == 0);
    }

    @Test
    void testISODateValidValue() {
        bean.isoDate = "2020-02-29";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        Assertions.assertTrue(violations.size() == 0);
    }

}
