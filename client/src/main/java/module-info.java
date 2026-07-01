module arkheim.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens arkheim.client.Presentation to javafx.fxml;
    exports arkheim.client.Presentation;
    exports arkheim.client;
}
