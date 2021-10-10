package io.xlate.validation.internal.constraintvalidators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.el.ELProcessor;
import javax.el.PropertyNotFoundException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import javax.validation.ValidationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import io.xlate.validation.constraints.JdbcStatement;

@ExtendWith(MockitoExtension.class)
class JdbcStatementValidatorTest {

    JdbcStatementValidator target;

    @Mock
    ConstraintValidatorContext constraintContext;

    @BeforeEach
    void setUp() {
        target = new JdbcStatementValidator();
    }

    @Test
    void testWhenExpessionFalse() throws NamingException {
        JdbcStatement annotation = Mockito.mock(JdbcStatement.class);
        Mockito.when(annotation.value()).thenReturn("SELECT 1");
        Mockito.when(annotation.when()).thenReturn("0 == 1");
        Mockito.when(annotation.dataSourceLookup()).thenReturn("");

        DataSource dataSource = Mockito.mock(DataSource.class);
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        System.setProperty("org.osjava.sj.delimiter", "/");

        Context context = new InitialContext();
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.bind("java:comp/DefaultDataSource", dataSource);

        try {
            target.initialize(annotation);
            Assertions.assertTrue(target.isValid(new Object(), constraintContext));
        } finally {
            context.close();
        }
    }

    @Test
    void testGetDataSourceReturnsDefault() throws NamingException {
        DataSource dataSource = Mockito.mock(DataSource.class);
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        System.setProperty("org.osjava.sj.delimiter", "/");
        Context context = new InitialContext();
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.bind("java:comp/DefaultDataSource", dataSource);
        try {
            assertEquals(dataSource, JdbcStatementValidator.getDataSource(""));
        } finally {
            context.close();
        }
    }

    @Test
    void testGetDataSourceReturnsNamed() throws NamingException {
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        System.setProperty("org.osjava.sj.delimiter", "/");
        DataSource dataSource = Mockito.mock(DataSource.class);
        Context context = new InitialContext();
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.createSubcontext("java:comp/env");
        context.createSubcontext("java:comp/env/jdbc");
        String lookup = "java:comp/env/jdbc/testDataSource";
        context.bind(lookup, dataSource);
        try {
            assertEquals(dataSource, JdbcStatementValidator.getDataSource(lookup));
        } finally {
            context.close();
        }
    }

    @Test
    void testGetDataSourceThrowsValidationExceptionCausedByNamingException() throws NamingException {
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        System.setProperty("org.osjava.sj.delimiter", "/");
        Context context = new InitialContext();
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.createSubcontext("java:comp/env");
        context.createSubcontext("java:comp/env/jdbc");

        String lookup = "java:comp/env/jdbc/testDataSource";
        ValidationException ex;
        try {
            ex = assertThrows(ValidationException.class, () -> {
                JdbcStatementValidator.getDataSource(lookup);
            });
        } finally {
            context.close();
        }

        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof NamingException);
    }

    @Test
    void testExecuteQuerySucceeds() throws SQLException {
        String sql = "SELECT 1";
        String[] parameters = { };

        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);

        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        Mockito.when(connection.prepareStatement(sql)).thenReturn(statement);

        ResultSet results  = Mockito.mock(ResultSet.class);
        Mockito.when(statement.executeQuery()).thenReturn(results);
        Mockito.when(results.next()).thenReturn(true);

        target.dataSource = dataSource;
        assertTrue(target.executeQuery(null, sql, parameters));
    }

    @Test
    void testExecuteQueryThrowsValidationException() throws SQLException {
        DataSource dataSource = Mockito.mock(DataSource.class);
        String sql = "SELECT 1";
        String[] parameters = { };
        Mockito.when(dataSource.getConnection()).thenThrow(SQLException.class);
        target.dataSource = dataSource;
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            target.executeQuery(null, sql, parameters);
        });

        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertEquals(SQLException.class, cause.getClass());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testSetParametersNoParameters() throws SQLException {
        String[] parameters = { };
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        final AtomicInteger callCount = new AtomicInteger(0);

        Mockito.doAnswer((Answer<?>) invocation -> {
            callCount.incrementAndGet();
            return null;
        }).when(statement).setObject(1, Object.class);

        target.setParameters(null, parameters, statement);
        assertEquals(0, callCount.get());
    }

    public static class TestSetParametersSucceeds {
        public String getValue1() {
            return "value1";
        }

        public int getValue2() {
            return 2;
        }
    }

    @Test
    void testSetParametersSucceeds() throws SQLException {
        TestSetParametersSucceeds self = new TestSetParametersSucceeds();
        String[] parameters = { "self.value1", "self.value2" };
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        final AtomicInteger callCount = new AtomicInteger(0);

        Mockito.doAnswer((Answer<?>) invocation -> {
            callCount.incrementAndGet();
            return null;
        }).when(statement).setObject(1, self.getValue1());

        Mockito.doAnswer((Answer<?>) invocation -> {
            callCount.incrementAndGet();
            return null;
        }).when(statement).setObject(2, self.getValue2());

        ELProcessor processor = new ELProcessor();
        processor.defineBean("self", self);

        target.setParameters(processor, parameters, statement);
        assertEquals(2, callCount.get());
    }

    public static class TestSetParametersPropertyNotFound {
        public String getValue1() {
            return "value1";
        }

        @SuppressWarnings("unused")
        private int getValue2() {
            return 2;
        }
    }

    @Test
    void testSetParametersPropertyNotFound() {
        TestSetParametersPropertyNotFound self = new TestSetParametersPropertyNotFound();
        String[] parameters = { "self.value1", "self.value2" };
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);

        ELProcessor processor = new ELProcessor();
        processor.defineBean("self", self);

        ConstraintDeclarationException ex = assertThrows(ConstraintDeclarationException.class, () -> {
            target.setParameters(processor, parameters, statement);
        });

        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertEquals(PropertyNotFoundException.class, cause.getClass());
    }

    public static class TestSetParametersSQLException {
        // Intentionally left empty
    }

    @Test
    void testSetParametersSQLException() throws SQLException {
        TestSetParametersSQLException self = new TestSetParametersSQLException();
        String[] parameters = { "self" };
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        Mockito.doThrow(java.sql.SQLException.class).when(statement).setObject(1, self);

        ELProcessor processor = new ELProcessor();
        processor.defineBean("self", self);

        ConstraintDeclarationException ex = assertThrows(ConstraintDeclarationException.class, () -> {
            target.setParameters(processor, parameters, statement);
        });

        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertEquals(java.sql.SQLException.class, cause.getClass());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testUpdateValidationContextNodeEmpty() {
        AtomicBoolean disableDefaultConstraintViolationCalled;
        disableDefaultConstraintViolationCalled = new AtomicBoolean(false);

        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
        Mockito.doAnswer((Answer<?>) invocation -> {
            disableDefaultConstraintViolationCalled.set(true);
            return null;
        }).when(context).disableDefaultConstraintViolation();

        target.updateValidationContext(context, "", "No message");
        assertNotEquals(true, disableDefaultConstraintViolationCalled.get());
    }

    @Test
    void testUpdateValidationContextNodeHasName() {
        AtomicBoolean disableDefaultConstraintViolationCalled;

        disableDefaultConstraintViolationCalled = new AtomicBoolean(false);
        final Map<String, Object> values = new HashMap<>(2);

        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
        ConstraintViolationBuilder builder = Mockito.mock(ConstraintViolationBuilder.class);
        NodeBuilderCustomizableContext nodeBuilder = Mockito.mock(NodeBuilderCustomizableContext.class);

        final String message = "Test message";
        final String nodeName = "field1";

        Mockito.doAnswer((Answer<?>) invocation -> {
            disableDefaultConstraintViolationCalled.set(true);
            return null;
        }).when(context).disableDefaultConstraintViolation();

        Mockito.when(context.buildConstraintViolationWithTemplate(message)).thenAnswer((Answer<?>) invocation -> {
            values.put("message", invocation.getArgument(0));
            return builder;
        });
        Mockito.when(builder.addPropertyNode(nodeName)).thenAnswer((Answer<?>) invocation -> {
            values.put("nodeName", invocation.getArgument(0));
            return nodeBuilder;
        });

        Mockito.when(nodeBuilder.addConstraintViolation()).thenReturn(context);

        target.updateValidationContext(context, nodeName, message);

        assertTrue(disableDefaultConstraintViolationCalled.get());
        assertEquals(message, values.get("message"));
        assertEquals(nodeName, values.get("nodeName"));
    }

}
