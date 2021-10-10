package io.xlate.validation.internal.constraintvalidators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidatorContext;

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

import io.xlate.validation.constraints.DateTime;

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

    @ParameterizedTest
    @CsvSource(
        nullValues = "-",
        value = {
            "-,          false, true", // Null is valid
            "'',         false, true", // Empty is valid
            "' ',        false, false", // Blank is not valid
            "2018-01-01, false, true", // Valid value
            "2018-02-29, false, false", // Value not valid when not lenient
            "2018-02-29, true,  true" // Value is valid when lenient
        })
    void testIsValid(String inputSequence, boolean lenient, boolean expectation) {
        Mockito.when(annotation.patterns()).thenReturn(new String[] { "yyyy-MM-dd" });
        Mockito.when(annotation.lenient()).thenReturn(lenient);
        target.initialize(annotation);
        assertEquals(expectation, target.isValid(inputSequence, context));
    }

}
