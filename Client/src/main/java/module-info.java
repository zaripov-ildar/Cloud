module ru.starstreet.cloud.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires Core;
    requires io.netty.transport;
    requires io.netty.codec;
    requires org.slf4j;
    requires lombok;
    requires org.json;
    requires io.netty.buffer;
    requires io.netty.handler;


    opens ru.starstreet.cloud.client to javafx.fxml;
    exports ru.starstreet.cloud.client;
}