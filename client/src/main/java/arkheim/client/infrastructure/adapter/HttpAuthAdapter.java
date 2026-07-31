package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.AuthPort;
import arkheim.client.domain.dtos.User.response.UserDto;
import arkheim.client.infrastructure.ApiClient;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;

public class HttpAuthAdapter extends ApiClient implements AuthPort {

    @Override
    public UserDto login(String email, String rawPassword) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("rawPassword", rawPassword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request, UserDto.class, "Auth");
    }

    @Override
    public UserDto register(String username, String name, String rawPassword, String email, LocalDateTime dateOfBirth) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("name", name);
        body.addProperty("rawPassword", rawPassword);
        body.addProperty("email", email);
        if (dateOfBirth != null) {
            body.addProperty("dateOfBirth", dateOfBirth.toString());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request, UserDto.class, "Auth");
    }
}
