package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.dtos.User.response.UserDto;
import arkheim.client.domain.ports.FollowPort;
import arkheim.client.infrastructure.ApiClient;
import arkheim.client.domain.dtos.ApiResponse;
import arkheim.client.domain.dtos.User.response.IsFollowingDto;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.UUID;

public class HttpFollowAdapter extends ApiClient implements FollowPort {

    @Override
    public ApiResponse followUser(UUID followerId, UUID followingId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/" + followerId + "/to/" + followingId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return send(request, "Follow");
    }

    @Override
    public ApiResponse unfollowUser(UUID followerId, UUID followingId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/" + followerId + "/to/" + followingId))
                .DELETE()
                .build();

        return send(request, "Follow");
    }

    @Override
    public IsFollowingDto isFollowing(UUID followerId, UUID followingId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/" + followerId + "/checking/" + followingId))
                .GET()
                .build();

        return send(request, IsFollowingDto.class, "Follow");
    }

    @Override
    public List<UserDto> getFollowers(UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/followers/" + userId))
                .GET()
                .build();

        return sendList(request, UserDto.class, "Follow");
    }

    @Override
    public List<UserDto> getFollowing(UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/follows/following/" + userId))
                .GET()
                .build();

        return sendList(request, UserDto.class, "Follow");
    }
}
