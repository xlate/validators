package io.xlate.validation.internal.constraintvalidators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.xlate.validation.constraints.DateTime;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintValidatorContext;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DateTimeValidatorTest {

    DateTimeValidator target;

    @Mock
    ConstraintValidatorContext context;

    @Mock
    DateTime annotation;

    @BeforeEach
    void setUp() {
        target = new DateTimeValidator();
    }

    @Test
    void testMissingPatterns() {
        Mockito.when(annotation.patterns()).thenReturn(new String[0]);
        ConstraintDeclarationException ex = assertThrows(ConstraintDeclarationException.class, () -> {
            target.initialize(annotation);
        });
        assertEquals("At least one DateFormat pattern must be provided.", ex.getMessage());
    }

    @Test
    void testInvalidPattern() {
        Mockito.when(annotation.patterns()).thenReturn(new String[] { " NOT A VALID DATE PATTERN " });
        ConstraintDeclarationException ex = assertThrows(ConstraintDeclarationException.class, () -> {
            target.initialize(annotation);
        });
        assertTrue(ex.getMessage().startsWith("Invalid format pattern "));
    }

    @Test
    void testSequenceNull() {
        Mockito.when(annotation.patterns()).thenReturn(new String[] { "yyyy-MM-dd" });
        target.initialize(annotation);
        assertTrue(target.isValid(null, context));
    }

    @Test
    void testSequenceEmpty() {
        Mockito.when(annotation.patterns()).thenReturn(new String[] { "yyyy-MM-dd" });
        target.initialize(annotation);
        assertTrue(target.isValid("", context));
    }

    @Test
    void testSequenceSpaces() {
        Mockito.when(annotation.patterns()).thenReturn(new String[] { "yyyy-MM-dd" });
        target.initialize(annotation);
        assertTrue(!target.isValid("  ", context));
    }

    @Test
    void testSequenceValid() {
        Mockito.when(annotation.patterns()).thenReturn(new String[] { "yyyy-MM-dd" });
        target.initialize(annotation);
        assertTrue(target.isValid("2018-01-01", context));
    }

    @Test
    void testSequenceInvalidForNonLeapYear() {
        Mockito.when(annotation.patterns()).thenReturn(new String[] { "yyyy-MM-dd" });
        target.initialize(annotation);
        assertTrue(!target.isValid("2018-02-29", context));
    }

    @Test
    void testSequenceValidForNonLeapYearWhenLenient() {
        Mockito.when(annotation.patterns()).thenReturn(new String[] { "yyyy-MM-dd" });
        Mockito.when(annotation.lenient()).thenReturn(true);
        target.initialize(annotation);
        assertTrue(target.isValid("2018-02-29", context));
    }

}
