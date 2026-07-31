package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.dtos.ApiResponse;
import arkheim.client.domain.dtos.Post.response.PostDetailDto;
import arkheim.client.domain.ports.PostPort;
import arkheim.client.infrastructure.ApiClient;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class HttpPostAdapter extends ApiClient implements PostPort {

    @Override
    public PostDetailDto createPost(UUID authorId, String content, String mediaUrl, UUID parentPostId) {
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

        return send(request, PostDetailDto.class, "Post");
    }

    @Override
    public ApiResponse deletePost(UUID postId, UUID requesterId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/" + postId + "?requesterId=" + requesterId))
                .DELETE()
                .build();

        return send(request, "Post");
    }

    @Override
    public ApiResponse toggleLike(UUID postId, UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/" + postId + "/like?userId=" + userId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return send(request, "Post");
    }

    @Override
    public List<PostDetailDto> getUserTimeline(UUID requesterId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/timeline" + "?requesterId=" + requesterId))
                .GET()
                .build();

        return sendList(request, PostDetailDto.class, "Post");
    }

    @Override
    public PostDetailDto getPostDetails(UUID postId, UUID requesterId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/" + postId + "?requesterId=" + requesterId))
                .GET()
                .build();

        return send(request, PostDetailDto.class, "Post");
    }

    @Override
    public List<PostDetailDto> getPostReplies(UUID postId, UUID requesterId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/" + postId + "/replies?requesterId=" + requesterId))
                .GET()
                .build();

        return sendList(request, PostDetailDto.class, "Post");
    }

    @Override
    public List<PostDetailDto> findPostsByWord(String word, UUID requesterId) {
        if (word == null || word.isBlank()) {
            return java.util.Collections.emptyList();
        }

        String cleanWord = word.trim();
        if (cleanWord.startsWith("#")) {
            cleanWord = cleanWord.substring(1).trim();
        }

        if (cleanWord.isBlank()) {
            return java.util.Collections.emptyList();
        }

        String encodedWord = java.net.URLEncoder.encode(cleanWord, StandardCharsets.UTF_8); // since URIs can only contain normal ASCII characters, it encodes the hashtag.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/byword/" + encodedWord + "?requesterId=" + requesterId))
                .GET()
                .build();

        return sendList(request, PostDetailDto.class, "Post");
    }

    @Override
    public List<PostDetailDto> getUserPosts(String username) {
        return getUserPosts(username, null);
    }

    @Override
    public List<PostDetailDto> getUserPosts(String username, UUID requesterId) {
        if (username == null || username.isBlank()) {
            return Collections.emptyList();
        }
        String encodedUsername = URLEncoder.encode(username.trim(), StandardCharsets.UTF_8);
        String uriStr = baseUrl + "/api/posts/user/" + encodedUsername;
        if (requesterId != null) {
            uriStr += "?requesterId=" + requesterId;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .GET()
                .build();

        return sendList(request, PostDetailDto.class, "Post");
    }
}
