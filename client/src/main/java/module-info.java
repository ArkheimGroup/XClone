module arkheim.client {
    requires javafx.controls;
    requires javafx.fxml;
    opens arkheim.client.presentation.controllers to javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;

    opens arkheim.client.presentation to javafx.fxml;
    opens arkheim.client.presentation.Assets to javafx.graphics, javafx.fxml;
    opens arkheim.client.presentation.views to javafx.fxml;

    // Gson parsing
    opens arkheim.client.domain.dtos to com.google.gson;
    opens arkheim.client.infrastructure.exception to com.google.gson;

    exports arkheim.client.presentation;
    exports arkheim.client.presentation.utils;
    exports arkheim.client;

    // Domain ports — consumed by infrastructure adapters and presentation ViewModels
    exports arkheim.client.domain.ports;
    exports arkheim.client.domain.dtos;
    exports arkheim.client.domain.dtos.Hashtag.response;
    exports arkheim.client.domain.dtos.Media.response;
    exports arkheim.client.domain.dtos.Media.request;
    exports arkheim.client.domain.dtos.Post.response;
    exports arkheim.client.domain.dtos.Post.request;
    exports arkheim.client.domain.dtos.User.response;
    exports arkheim.client.domain.dtos.User.request;

    // Infrastructure adapters — HTTP and TCP implementations of domain ports
    exports arkheim.client.infrastructure.adapter;

    // Presentation Theme Mode — consumed by utils package
    exports arkheim.client.presentation.theme;
}
