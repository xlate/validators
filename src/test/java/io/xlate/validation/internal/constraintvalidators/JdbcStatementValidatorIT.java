package io.xlate.validation.internal.constraintvalidators;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.xlate.validation.constraints.JdbcStatement;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
class JdbcStatementValidatorIT {

    private static final String CUSTOM_DATASOURCE = "java:comp/env/jdbc/testDataSource";

    @Mock
    DataSource dataSource;

    Validator validator;
    Context context;

    static class TestBeanDefaultDataSource {
        @JdbcStatement("SELECT 1 FROM FIELD_1_TABLE WHERE COL1 = 'value1'")
        String field1;
    }

    static class TestBeanCustomDataSource {
        @JdbcStatement(
                value = "SELECT 1 FROM FIELD_1_TABLE WHERE COL1 = ?",
                dataSourceLookup = CUSTOM_DATASOURCE,
                parameters = { "self" })
        String field1;
    }

    @JdbcStatement(
            value = "SELECT 1 FROM FIELD_1_TABLE WHERE COL1 = ? AND COL2 = ?",
            dataSourceLookup = CUSTOM_DATASOURCE,
            parameters = { "self.field1", "self.field2" },
            node = "field1",
            message = "field1 failed")
    public static class TestBeanCustomDataSourceCrossField {
        String field1;
        String field2;

        public String getField1() {
            return field1;
        }

        public String getField2() {
            return field2;
        }
    }

    @BeforeAll
    static void setUpBeforeClass() throws ClassNotFoundException {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:derby:memory:testDB;create=true");
    }

    @BeforeEach
    void setUp() throws NamingException {
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        System.setProperty("org.osjava.sj.delimiter", "/");

        context = new InitialContext();
    }

    @AfterEach
    void tearDown() throws NamingException {
        context.close();
        System.getProperties().remove("java.naming.factory.initial");
        System.getProperties().remove("org.osjava.sj.jndi.shared");
        System.getProperties().remove("org.osjava.sj.delimiter");
    }

    @Test
    void testValidationOfField1UsingDefaultDataSourceSucceeds() throws NamingException, SQLException {
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.bind("java:comp/DefaultDataSource", dataSource);
        Connection connection = getConnection();
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        TestBeanDefaultDataSource bean = new TestBeanDefaultDataSource();

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE FIELD_1_TABLE (COL1 VARCHAR(255))");
            statement.executeUpdate("INSERT INTO FIELD_1_TABLE (COL1) VALUES ('value1')");

            Set<ConstraintViolation<TestBeanDefaultDataSource>> violations = validator.validate(bean);
            Assertions.assertTrue(violations.isEmpty());
        } finally {
            try (Statement statement = getConnection().createStatement()) {
                statement.execute("DROP TABLE FIELD_1_TABLE");
            }
        }
    }

    @Test
    void testValidationOfField1UsingDefaultDataSourceFails() throws NamingException, SQLException {
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.bind("java:comp/DefaultDataSource", dataSource);

        Connection connection = getConnection();
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        TestBeanDefaultDataSource bean = new TestBeanDefaultDataSource();

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE FIELD_1_TABLE (COL1 VARCHAR(255))");
            statement.executeUpdate("INSERT INTO FIELD_1_TABLE (COL1) VALUES ('NOT value1')");

            Set<ConstraintViolation<TestBeanDefaultDataSource>> violations = validator.validate(bean);
            Assertions.assertTrue(violations.size() == 1);
        } finally {
            try (Statement statement = getConnection().createStatement()) {
                statement.execute("DROP TABLE FIELD_1_TABLE");
            }
        }
    }

    @Test
    void testValidationOfField1UsingCustomDataSourceAndParameterSucceeds() throws NamingException, SQLException {
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.createSubcontext("java:comp/env");
        context.createSubcontext("java:comp/env/jdbc");
        context.bind(CUSTOM_DATASOURCE, dataSource);
        Connection connection = getConnection();
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        TestBeanCustomDataSource bean = new TestBeanCustomDataSource();
        String expectedValue = "expected1";
        bean.field1 = expectedValue;

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE FIELD_1_TABLE (COL1 VARCHAR(255))");
            statement.executeUpdate("INSERT INTO FIELD_1_TABLE (COL1) VALUES ('" + expectedValue + "')");

            Set<ConstraintViolation<TestBeanCustomDataSource>> violations = validator.validate(bean);
            Assertions.assertTrue(violations.isEmpty());
        } finally {
            try (Statement statement = getConnection().createStatement()) {
                statement.execute("DROP TABLE FIELD_1_TABLE");
            }
        }
    }

    @Test
    void testValidationOfField1UsingUnknownDataSourceThrowsNamingException() throws NamingException, SQLException {
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.createSubcontext("java:comp/env");
        context.createSubcontext("java:comp/env/jdbc");

        Connection connection = getConnection();
        TestBeanCustomDataSource bean = new TestBeanCustomDataSource();

        try (Statement statement = connection.createStatement()) {
            ValidationException ex;

            try {
                ex = assertThrows(ValidationException.class, () -> {
                    validator.validate(bean);
                });
            } finally {
                context.close();
            }

            Throwable cause = ex;
            while ((cause = cause.getCause()) != null) {
                if (cause instanceof NamingException) {
                    break;
                }
            }
            assertTrue(cause != null);
            assertTrue(cause instanceof NamingException);
        }
    }

    @Test
    void testCrossFieldValidationUsingCustomDataSourceAndParametersSucceeds() throws NamingException, SQLException {
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.createSubcontext("java:comp/env");
        context.createSubcontext("java:comp/env/jdbc");
        context.bind(CUSTOM_DATASOURCE, dataSource);
        Connection connection = getConnection();
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        TestBeanCustomDataSourceCrossField bean = new TestBeanCustomDataSourceCrossField();
        String expectedValue1 = "expected1";
        String expectedValue2 = "expected2";
        bean.field1 = expectedValue1;
        bean.field2 = expectedValue2;
        Set<ConstraintViolation<TestBeanCustomDataSourceCrossField>> violations;

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE FIELD_1_TABLE (COL1 VARCHAR(255), COL2 VARCHAR(255))");
            statement.executeUpdate("INSERT INTO FIELD_1_TABLE (COL1,COL2) VALUES ('"
                    + expectedValue1 + "','"
                    + expectedValue2 + "')");

            violations = validator.validate(bean);

        } finally {
            try (Statement statement = getConnection().createStatement()) {
                statement.execute("DROP TABLE FIELD_1_TABLE");
            }
        }

        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    void testCrossFieldValidationUsingCustomDataSourceAndParametersFails() throws NamingException, SQLException {
        context.createSubcontext("java:");
        context.createSubcontext("java:comp");
        context.createSubcontext("java:comp/env");
        context.createSubcontext("java:comp/env/jdbc");
        context.bind(CUSTOM_DATASOURCE, dataSource);
        Connection connection = getConnection();
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        TestBeanCustomDataSourceCrossField bean = new TestBeanCustomDataSourceCrossField();
        String expectedValue1 = "validvalue1";
        String expectedValue2 = "validvalue2";
        bean.field1 = "invalidvalue1";
        bean.field2 = expectedValue2;
        Set<ConstraintViolation<TestBeanCustomDataSourceCrossField>> violations;

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE FIELD_1_TABLE (COL1 VARCHAR(255), COL2 VARCHAR(255))");
            statement.executeUpdate("INSERT INTO FIELD_1_TABLE (COL1,COL2) VALUES ('"
                    + expectedValue1 + "','"
                    + expectedValue2 + "')");

            violations = validator.validate(bean);

        } finally {
            try (Statement statement = getConnection().createStatement()) {
                statement.execute("DROP TABLE FIELD_1_TABLE");
            }
        }

        Assertions.assertEquals(1, violations.size());
        ConstraintViolation<TestBeanCustomDataSourceCrossField> v1 = violations.iterator().next();
        Assertions.assertEquals("field1", v1.getPropertyPath().toString());
        Assertions.assertEquals("field1 failed", v1.getMessage());
    }
}
