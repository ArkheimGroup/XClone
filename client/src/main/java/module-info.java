module arkheim.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires com.google.gson;

    opens arkheim.client.presentation to javafx.fxml;

    exports arkheim.client.presentation;
    exports arkheim.client;

    // Domain ports — consumed by infrastructure adapters and presentation ViewModels
    exports arkheim.client.domain.ports;
    exports arkheim.client.domain.ports.dtos;

    // Infrastructure adapters — HTTP and TCP implementations of domain ports
    exports arkheim.client.infrastructure.adapter;
}
