package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.MediaPort;
import arkheim.client.domain.ports.dtos.MediaDto;
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

public class HttpMediaAdapter extends ApiClient implements MediaPort {

    @Override
    public MediaDto registerMedia(String url, int width, int height, long fileSize, UUID uploadedBy) {
        JsonObject body = new JsonObject();
        body.addProperty("url", url);
        body.addProperty("width", width);
        body.addProperty("height", height);
        body.addProperty("fileSize", fileSize);
        body.addProperty("uploadedBy", uploadedBy.toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/media"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request, MediaDto.class, "Media");
    }

    @Override
    public void linkMediaToPost(UUID mediaId, UUID postId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/media/" + mediaId + "/link/" + postId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        send(request, "Media");
    }

    @Override
    public void unlinkMediaFromPost(UUID mediaId, UUID postId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/media/" + mediaId + "/link/" + postId))
                .DELETE()
                .build();

        send(request, "Media");
    }

    @Override
    public void deleteMedia(UUID mediaId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/media/" + mediaId))
                .DELETE()
                .build();

        send(request, "Media");
    }

    @Override
    public List<MediaDto> getMediaForPost(UUID postId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/media/post/" + postId))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Media request failed with HTTP " + response.statusCode() + ": " + response.body());
            }
            return gson.fromJson(response.body(), MEDIA_LIST_TYPE);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Media HTTP request failed", e);
        }
    }
}
