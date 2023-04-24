package io.xlate.validation.internal.constraintvalidators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.format.ResolverStyle;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @ValueSource(strings = { "JAVA_TEXT", "JAVA_TIME" })
    void testInvalidPattern(DateTime.ParserType type) {
        Mockito.when(annotation.parserType()).thenReturn(type);
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
            "yyyy-MM-dd, -,             JAVA_TEXT, SMART,   true",  // Null is valid
            "yyyy-MM-dd, '',            JAVA_TEXT, SMART,   true",  // Empty is valid
            "yyyy-MM-dd, ' ',           JAVA_TEXT, SMART,   false", // Blank is not valid
            "yyyy-MM-dd, 2018-01-01,    JAVA_TEXT, SMART,   true",  // Valid value
            "yyyy-MM-dd, 2018-02-29,    JAVA_TEXT, SMART,   false", // Value not valid when not lenient
            "yyyy-MM-dd, 2018-02-29,    JAVA_TEXT, LENIENT, true",  // Value is valid when lenient
            "yyyy-MM-dd, 2018-1-1 >,    JAVA_TEXT, SMART,   true",  // Value is valid with java.text parsing!?
            "yyyy-MM-dd, 2018-1-1 >,    JAVA_TEXT, LENIENT, true",  // Value is valid with java.text parsing!?
            "yyyy-MM-dd, 2018-02-29 >>, JAVA_TIME, SMART,   false", // Value is invalid with java.time parsing
            "yyyy-MM-dd, 2018-02-29,    JAVA_TIME, STRICT,  true",  // Value is valid with java.time parsing and year-of-era (y)
            "uuuu-MM-dd, 2018-02-29,    JAVA_TIME, STRICT,  false", // Value is valid with java.time parsing and year (u)
        })
    void testIsValid(String pattern, String inputSequence, DateTime.ParserType type, ResolverStyle resolverStyle, boolean isValid) {
        Mockito.when(annotation.parserType()).thenReturn(type);
        Mockito.when(annotation.patterns()).thenReturn(new String[] { pattern });
        Mockito.when(annotation.resolverStyle()).thenReturn(resolverStyle);
        Mockito.when(annotation.lenient()).thenReturn(resolverStyle == ResolverStyle.LENIENT);

        target.initialize(annotation);
        assertEquals(isValid, target.isValid(inputSequence, context));
    }

}
