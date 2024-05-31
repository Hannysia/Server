module annaKnysh.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.java_websocket;
    requires jakarta.xml.bind;
    requires java.sql;
    requires javafx.graphics;
    opens annaKnysh.serverside.view to javafx.fxml;
    opens annaKnysh.serverside.controller to javafx.fxml, jakarta.xml.bind;
    opens annaKnysh.serverside.model to javafx.fxml, jakarta.xml.bind;
    exports annaKnysh.serverside.view;
    exports annaKnysh.serverside.model;
    exports annaKnysh.serverside.controller;
    exports annaKnysh.serverside.chat;
    exports annaKnysh.serverside.database;
    exports annaKnysh.serverside.xml;
    opens annaKnysh.serverside.xml.message to jakarta.xml.bind;
    opens annaKnysh.serverside.xml to jakarta.xml.bind;
    opens annaKnysh.serverside.xml.auth to jakarta.xml.bind;
    opens annaKnysh.serverside.xml.chat to jakarta.xml.bind;
    exports annaKnysh.serverside.xml.message;
    exports annaKnysh.serverside.xml.chat;
    exports annaKnysh.serverside.xml.auth;
    exports annaKnysh.serverside.xml.auxiliary;

}