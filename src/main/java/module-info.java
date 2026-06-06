module MonopolyDealCard.main {
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires org.eclipse.jetty.websocket.javax.server;
    requires jetty.websocket.api;  // 专门解决 javax.websocket 的报错
    requires com.fasterxml.jackson.datatype.jsr310;

    opens edu.group10.common.model to com.fasterxml.jackson.databind;

    opens edu.group10.FrontEnd.controller to javafx.fxml;
    opens edu.group10.FrontEnd to javafx.fxml;


    exports edu.group10.FrontEnd;
}