package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.MediaPort;
import arkheim.client.domain.ports.dtos.MediaDto;
import arkheim.client.infrastructure.ApiClient;
import com.google.gson.JsonObject;

import java.io.File;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.file.Files;
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
    public MediaDto uploadMedia(File file, UUID uploadedBy) {
        try {
            String boundary = "---Boundary" + System.currentTimeMillis();
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            String fileHeader = "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n"
                    + "Content-Type: " + mimeType + "\r\n\r\n";

            String fieldsAndFooter = "\r\n--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"uploadedBy\"\r\n\r\n"
                    + (uploadedBy != null ? uploadedBy : "")
                    + "\r\n--" + boundary + "--\r\n";

            HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.concat(
                    HttpRequest.BodyPublishers.ofString(fileHeader),
                    HttpRequest.BodyPublishers.ofFile(file.toPath()),
                    HttpRequest.BodyPublishers.ofString(fieldsAndFooter)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/media/upload"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(bodyPublisher)
                    .build();

            return send(request, MediaDto.class, "Media Upload");
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload media file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MediaDto> getMediaForPost(UUID postId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/media/post/" + postId))
                .GET()
                .build();

        return send(request, MEDIA_LIST_TYPE, "Media");
    }
}
