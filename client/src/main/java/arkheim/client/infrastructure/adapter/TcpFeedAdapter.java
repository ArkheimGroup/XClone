package arkheim.client.infrastructure.adapter;

import arkheim.client.domain.ports.FeedPort;
import arkheim.client.domain.ports.dtos.PostDto;
import arkheim.client.infrastructure.ApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import arkheim.client.infrastructure.LocalDateTimeAdapter;

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
    public List<PostDto> getHomeFeed(UUID userId) {
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
                throw new RuntimeException("Empty response from feed server");
            }
            if (jsonResponse.startsWith("Error:")) {
                throw new RuntimeException("Feed server error: " + jsonResponse);
            }

            return gson.fromJson(jsonResponse, ApiClient.getPostListType());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("TCP feed request failed for user " + userId, e);
        }
    }
}
