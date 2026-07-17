package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.PostPort;
import arkheim.client.domain.ports.dtos.PostDto;
import arkheim.client.infrastructure.ApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

public class HttpPostAdapter extends ApiClient implements PostPort {

    @Override
    public PostDto createPost(UUID authorId, String content, String mediaUrl, UUID parentPostId) {
        JsonObject body = new JsonObject();
        body.addProperty("authorId", authorId.toString());
        body.addProperty("content", content);
        body.addProperty("mediaUrl", mediaUrl);
        if (parentPostId != null) {
            body.addProperty("parentPostId", parentPostId.toString());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request, PostDto.class, "Post");
    }

    @Override
    public void deletePost(UUID postId, UUID requesterId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/" + postId + "?requesterId=" + requesterId))
                .DELETE()
                .build();

        send(request, "Post");
    }

    @Override
    public void toggleLike(UUID postId, UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/" + postId + "/like?userId=" + userId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        send(request, "Post");
    }

    @Override
    public List<PostDto> getUserTimeline(UUID requesterId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/timeline" + "?requesterId=" + requesterId))
                .GET()
                .build();

        return send(request, POST_LIST_TYPE, "Post");
    }

    @Override
    public PostDto getPostDetails(UUID postId, UUID requesterId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/" + postId + "?requesterId=" + requesterId))
                .GET()
                .build();

        return send(request, PostDto.class, "Post");
    }

    @Override
    public List<PostDto> getPostReplies(UUID postId, UUID requesterId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/" + postId + "/replies?requesterId=" + requesterId))
                .GET()
                .build();

        return send(request, POST_LIST_TYPE, "Post");
    }
}
