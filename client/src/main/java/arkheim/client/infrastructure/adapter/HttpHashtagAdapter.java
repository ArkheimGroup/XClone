package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.HashtagPort;
import arkheim.client.domain.ports.dtos.HashtagDto;
import arkheim.client.domain.ports.dtos.PostDto;
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

public class HttpHashtagAdapter extends ApiClient implements HashtagPort {


    @Override
    public List<PostDto> getPostsByHashtag(String hashtagName, UUID requesterId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/hashtags/" + hashtagName + "/posts?requesterId=" + requesterId))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Hashtag request failed with HTTP " + response.statusCode() + ": " + response.body());
            }
            return gson.fromJson(response.body(), POST_LIST_TYPE);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Hashtag HTTP request failed", e);
        }
    }

    @Override
    public List<HashtagDto> getHashtagsForPost(UUID postId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/hashtags/posts/" + postId))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Hashtag request failed with HTTP " + response.statusCode() + ": " + response.body());
            }
            return gson.fromJson(response.body(), HASHTAG_LIST_TYPE);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Hashtag HTTP request failed", e);
        }
    }
}
