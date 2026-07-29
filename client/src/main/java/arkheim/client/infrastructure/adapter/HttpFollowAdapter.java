package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.FollowPort;
import arkheim.client.domain.ports.dtos.UserDto;
import arkheim.client.infrastructure.ApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

public class HttpFollowAdapter extends ApiClient implements FollowPort {

    @Override
    public void followUser(UUID followerId, UUID followingId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/" + followerId + "/to/" + followingId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        send(request, "Follow");
    }

    @Override
    public void unfollowUser(UUID followerId, UUID followingId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/" + followerId + "/to/" + followingId))
                .DELETE()
                .build();

        send(request, "Follow");
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followingId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/" + followerId + "/checking/" + followingId))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Follow request failed with HTTP " + response.statusCode() + ": " + response.body());
            }
            return Boolean.parseBoolean(response.body().trim());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Follow HTTP request failed", e);
        }
    }

    @Override
    public List<UserDto> getFollowers(UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/followers/" + userId))
                .GET()
                .build();

        return sendList(request);
    }

    @Override
    public List<UserDto> getFollowing(UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/following/" + userId))
                .GET()
                .build();

        return sendList(request);
    }

    private List<UserDto> sendList(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Follow request failed with HTTP " + response.statusCode() + ": " + response.body());
            }
            return gson.fromJson(response.body(), USER_LIST_TYPE);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Follow HTTP request failed", e);
        }
    }
}
