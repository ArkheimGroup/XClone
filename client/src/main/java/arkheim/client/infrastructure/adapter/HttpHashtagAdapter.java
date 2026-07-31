package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.dtos.Hashtag.response.HashtagDto;
import arkheim.client.domain.dtos.Post.response.PostDetailDto;
import arkheim.client.domain.ports.HashtagPort;
import arkheim.client.infrastructure.ApiClient;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.UUID;

public class HttpHashtagAdapter extends ApiClient implements HashtagPort {


    @Override
    public List<PostDetailDto> getPostsByHashtag(String hashtagName, UUID requesterId) {
        if (hashtagName == null || hashtagName.isBlank()) {
            return java.util.Collections.emptyList();
        }

        String cleanTag = hashtagName.trim();
        if (cleanTag.startsWith("#")) {
            cleanTag = cleanTag.substring(1).trim();
        }

        if (cleanTag.isBlank()) {
            return java.util.Collections.emptyList();
        }

        cleanTag = cleanTag.toLowerCase();

        String encodedTag = java.net.URLEncoder.encode(cleanTag, java.nio.charset.StandardCharsets.UTF_8);
        String queryParam = requesterId != null ? "?requesterId=" + requesterId : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/hashtags/" + encodedTag + "/posts" + queryParam))
                .GET()
                .build();

        return sendList(request, PostDetailDto.class, "Hashtag");
    }

    @Override
    public List<HashtagDto> getHashtagsForPost(UUID postId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/hashtags/posts/" + postId))
                .GET()
                .build();

        return sendList(request, HashtagDto.class, "Hashtag");
    }
}
