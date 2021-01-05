/*******************************************************************************
 * Copyright (C) 2018 xlate.io LLC, http://www.xlate.io
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package io.xlate.validation.internal.constraintvalidators;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import io.xlate.validation.constraints.DateTime;

public class DateTimeValidator implements ConstraintValidator<DateTime, CharSequence> {

    private List<DateFormat> formats;

    @Override
    public void initialize(DateTime constraintAnnotation) {
        final DateTime annotation = constraintAnnotation;
        final String[] patterns = annotation.patterns();

        if (patterns.length == 0) {
            throw new ConstraintDeclarationException("At least one DateFormat pattern must be provided.");
        }

        formats = new ArrayList<>(patterns.length);

        for (String pattern : patterns) {
            try {
                DateFormat format = new SimpleDateFormat(pattern);
                format.setLenient(annotation.lenient());
                formats.add(format);
            } catch (IllegalArgumentException e) {
                throw new ConstraintDeclarationException("Invalid format pattern `" + pattern + "`", e);
            }
        }
    }

    @Override
    public boolean isValid(CharSequence sequence, ConstraintValidatorContext context) {
        if (sequence == null || sequence.length() == 0) {
            return true;
        }

        final String value = sequence.toString();

        for (DateFormat format : formats) {
            // DateFormat is not thread-safe, clone a local copy before use.
            final DateFormat localFormat = (DateFormat) format.clone();

            try {
                localFormat.parse(value);
                return true;
            } catch (@SuppressWarnings("unused") ParseException e) {
                // Value does not match the pattern, ignore and continue.
            }
        }

        return false;
    }
}
