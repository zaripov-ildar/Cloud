module Server {
    requires io.netty.transport;
    requires io.netty.codec;
    requires org.slf4j;
    requires lombok;
    requires org.json;
    requires io.netty.buffer;
    requires Core;
    requires java.sql;
    requires com.h2database;
    requires io.netty.handler;
}