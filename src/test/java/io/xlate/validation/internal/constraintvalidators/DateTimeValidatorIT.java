package io.xlate.validation.internal.constraintvalidators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.xlate.validation.constraints.DateTime;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

@ExtendWith(MockitoExtension.class)
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
        assertEquals(1, violations.size());
        assertEquals("isoDate", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testISODateValidWhenLenient() {
        bean.isoDateLenient = "2018-02-29";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        assertEquals(0, violations.size());
    }

    @Test
    void testISODateValidValue() {
        bean.isoDate = "2020-02-29";
        Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
        assertEquals(0, violations.size());
    }

}
