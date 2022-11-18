module Core {
    requires lombok;
    requires org.json;
    requires java.sql;
    requires com.h2database;

    exports ru.starstreet.cloud.core;
    exports ru.starstreet.cloud.core.Utils;
}