package arkheim.client.infrastructure;

import arkheim.client.domain.ports.dtos.HashtagDto;
import arkheim.client.domain.ports.dtos.MediaDto;
import arkheim.client.domain.ports.dtos.PostDto;
import arkheim.client.domain.ports.dtos.UserDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

public abstract class ApiClient {
    protected final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    protected final HttpClient httpClient = HttpClient.newHttpClient();
    protected final String baseUrl = "http://127.0.0.1:8080"; // FIX ME : later on change this into a variable inside a dot file

    // JSON Return type used in HttpFollowAdapter
    protected static final Type USER_LIST_TYPE = new TypeToken<List<UserDto>>(){}.getType();
    protected static final Type POST_LIST_TYPE = new TypeToken<List<PostDto>>(){}.getType();
    protected static final Type HASHTAG_LIST_TYPE = new TypeToken<List<HashtagDto>>(){}.getType();
    protected static final Type MEDIA_LIST_TYPE = new TypeToken<List<MediaDto>>() {}.getType();

    public static Type getPostListType(){
        return POST_LIST_TYPE;
    }

    /**
     * @param context use domain layer entity names
     * @return Response converted to class with type {@link T}
     * @param <T> same as responseType
     */
    protected <T> T send(HttpRequest request, Type responseType, String context){
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkStatusHelper(response.statusCode(), response.body(), context);
            return gson.fromJson(response.body(), responseType);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(context + "HTTP Request failed" + e);
        }

    }

    /**
     * @param context use domain layer entity names
     */
    protected void send(HttpRequest request, String context) {
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            checkStatusHelper(response.statusCode(), null, context);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(context + " HTTP request failed", e);
        }
    }

    private void checkStatusHelper(int statusCode, String body, String context){
        if (statusCode < 200 || statusCode >= 300){
            String errMsg = context + " HTTP Request failed, error code: " + statusCode;
            if (body != null && !body.isBlank()){
                errMsg += ".\n Body: " + body;
            }

            throw new RuntimeException(errMsg);
        }
    }
}

