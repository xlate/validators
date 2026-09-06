package io.xlate.validation.internal.constraintvalidators;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import io.xlate.validation.constraints.DateTime;
import io.xlate.validation.constraints.DateTime.ParserType;

public class DateTimeValidator implements ConstraintValidator<DateTime, CharSequence> {

    private List<Object> formatters;
    private DateTime.ParserType formatterType;

    @Override
    public void initialize(DateTime constraintAnnotation) {
        formatterType = constraintAnnotation.parserType();

        final DateTime annotation = constraintAnnotation;
        final String[] patterns = annotation.patterns();

        if (patterns.length == 0) {
            throw new ConstraintDeclarationException("At least one DateFormat pattern must be provided.");
        }

        if (formatterType == ParserType.JAVA_TEXT) {
            formatters = Arrays.stream(patterns)
                    .map(p -> toJavaTextDateFormat(p, annotation))
                    .collect(Collectors.toList());
        } else {
            formatters = Arrays.stream(patterns)
                    .map(p -> toJavaTimeDateTimeFormatter(p, annotation))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public boolean isValid(CharSequence sequence, ConstraintValidatorContext context) {
        if (sequence == null || sequence.length() == 0) {
            return true;
        }

        if (formatterType == ParserType.JAVA_TEXT) {
            final String value = sequence.toString();

            for (DateFormat format : this.<DateFormat>formatters()) {
                // DateFormat is not thread-safe, clone a local copy before use.
                final DateFormat localFormat = (DateFormat) format.clone();

                try {
                    localFormat.parse(value);
                    return true;
                } catch (@SuppressWarnings("unused") ParseException e) {
                    // Value does not match the pattern, ignore and continue.
                }
            }
        } else {
            for (DateTimeFormatter formatter : this.<DateTimeFormatter>formatters()) {
                try {
                    formatter.parse(sequence);
                    return true;
                } catch (@SuppressWarnings("unused") DateTimeParseException e) {
                    // Value does not match the pattern, ignore and continue.
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> formatters() {
        return (List<T>) formatters;
    }

    private DateFormat toJavaTextDateFormat(String pattern, DateTime annotation) {
        try {
            DateFormat format = new SimpleDateFormat(pattern);
            format.setLenient(annotation.lenient());
            return format;
        } catch (IllegalArgumentException e) {
            throw new ConstraintDeclarationException("Invalid format pattern `" + pattern + "`", e);
        }
    }

    private DateTimeFormatter toJavaTimeDateTimeFormatter(String pattern, DateTime annotation) {
        try {
            return new DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .toFormatter()
                .withResolverStyle(annotation.resolverStyle());
        } catch (IllegalArgumentException e) {
            throw new ConstraintDeclarationException("Invalid format pattern `" + pattern + "`", e);
        }
    }
}
