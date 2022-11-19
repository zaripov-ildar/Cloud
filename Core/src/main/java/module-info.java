module Core {
    requires lombok;
    requires org.json;
    requires java.sql;
    requires com.h2database;
    requires io.netty.handler;
    requires org.slf4j;

    exports ru.starstreet.cloud.core;
    exports ru.starstreet.cloud.core.Utils;
}