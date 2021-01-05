module io.xlate.validation {

    requires java.instrument;
    requires java.logging;
    requires java.management;
    requires java.naming;
    requires java.prefs;
    requires java.sql;
    requires java.sql.rowset;

    requires static jakarta.el;
    requires transitive jakarta.validation;

    exports io.xlate.validation.constraints;

    opens io.xlate.validation.internal.constraintvalidators;

}
