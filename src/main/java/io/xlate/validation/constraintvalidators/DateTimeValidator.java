package io.xlate.validation.constraintvalidators;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.xlate.validation.constraints.DateTime;

public class DateTimeValidator implements ConstraintValidator<DateTime, CharSequence> {

    private DateTime annotation;

    @Override
    public void initialize(DateTime constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(CharSequence sequence, ConstraintValidatorContext context) {
        if (sequence == null || sequence.length() == 0) {
            return true;
        }

        final String value = sequence.toString();

        for (String pattern : annotation.patterns()) {
            final DateFormat format = new SimpleDateFormat(pattern);
            format.setLenient(annotation.lenient());

            try {
                format.parse(value);
                return true;
            } catch (@SuppressWarnings("unused") ParseException e) {
                continue;
            }

        }
        return false;
    }
}
