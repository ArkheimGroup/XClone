module arkheim.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens arkheim.client.presentation to javafx.fxml;

    exports arkheim.client.presentation;
    exports arkheim.client;

    // Domain ports — consumed by infrastructure adapters and presentation ViewModels
    exports arkheim.client.domain.ports;
    exports arkheim.client.domain.ports.dtos;
}
