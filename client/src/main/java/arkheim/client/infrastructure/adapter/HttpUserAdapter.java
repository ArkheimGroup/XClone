package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.UserPort;
import arkheim.client.domain.ports.dtos.UserProfileDto;
import arkheim.client.infrastructure.ApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public class HttpUserAdapter extends ApiClient implements UserPort {

    @Override
    public UserProfileDto getUserProfileById(UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users/" + userId))
                .GET()
                .build();

        return send(request, UserProfileDto.class, "User");
    }

    @Override
    public UserProfileDto getUserProfileByUsername(String username) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users/username/" + username))
                .GET()
                .build();

        return send(request, UserProfileDto.class, "User");
    }

    @Override
    public UserProfileDto updateProfile(UUID userId, String name, String biography, String pfpUrl, String bannerUrl, boolean isVerified, LocalDateTime dateOfBirth) {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId.toString());
        body.addProperty("name", name);
        body.addProperty("biography", biography);
        body.addProperty("pfpUrl", pfpUrl);
        body.addProperty("bannerUrl", bannerUrl);
        body.addProperty("isVerified", isVerified);
        body.addProperty("dateOfBirth", dateOfBirth != null ? dateOfBirth.toString() : null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request, UserProfileDto.class, "User");
    }

    @Override
    public void pinPost(UUID userId, UUID postId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users/" + userId + "/pin/" + postId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        send(request, "User");
    }

    @Override
    public void unpinPost(UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users/" + userId + "/pin"))
                .DELETE()
                .build();

        send(request, "User");
    }

    @Override
    public void deleteUser(UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users/" + userId))
                .DELETE()
                .build();

        send(request, "User");
    }
}
