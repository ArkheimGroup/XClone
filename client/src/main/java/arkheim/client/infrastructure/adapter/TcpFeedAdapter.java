package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.dtos.ApiResponse;
import arkheim.client.domain.dtos.Post.response.PostDetailDto;
import arkheim.client.domain.ports.FeedPort;
import arkheim.client.infrastructure.exception.ApiException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import arkheim.client.infrastructure.LocalDateTimeAdapter;
import com.google.gson.reflect.TypeToken;

public class TcpFeedAdapter implements FeedPort {

    private final String host;
    private final int port;
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public TcpFeedAdapter(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public List<PostDetailDto> getHomeFeed(UUID userId) {
        try (
            Socket socket = new Socket(host, port);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Send userId to server
            writer.println(userId.toString());

            // Read the JSON response
            String jsonResponse = reader.readLine();
            if (jsonResponse == null || jsonResponse.isBlank()) {
                throw new ApiException(500, buildException("Empty response from feed server"));
            }
            if (jsonResponse.startsWith("Error:")) {
                throw new ApiException(500, buildException("Feed server error: " + jsonResponse));
            }

            Type responseType = new TypeToken<List<PostDetailDto>>(){}.getType();
            return gson.fromJson(jsonResponse, responseType);
        } catch (Exception e) {
            throw new ApiException(500, buildException("TCP feed request failed for user " + userId + "\n" + e.getMessage()));
        }
    }

    private ApiResponse buildException(String msg) {
        ApiResponse exception = new ApiResponse();
        exception.setSuccess(false);
        exception.setMessage(msg);

        return exception;
    }
}
