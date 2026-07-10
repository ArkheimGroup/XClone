module arkheim.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens arkheim.client.presentation to javafx.fxml;
    exports arkheim.client.presentation;
    exports arkheim.client;
}
