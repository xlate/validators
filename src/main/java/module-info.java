module io.xlate.validation {

    requires java.logging;

    requires static java.naming;
    requires static java.sql;

    requires static jakarta.el;
    requires transitive java.validation;

    exports io.xlate.validation.constraints;

    opens io.xlate.validation.internal.constraintvalidators;

}
