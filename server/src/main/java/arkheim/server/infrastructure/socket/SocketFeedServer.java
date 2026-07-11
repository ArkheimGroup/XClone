package arkheim.server.infrastructure.socket;

import arkheim.server.application.dtos.responses.PostResponse;
import arkheim.server.application.services.TimelineService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SocketFeedServer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SocketFeedServer.class);

    private final TimelineService timelineService;
    private final Gson gson;

    /* executerService is used to benefit from thread pool
       compared to raw threads, i uses much less resource
    */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Value("${socket.feed.port:8082}") // Configurable in application.properties
    private int port;

    public SocketFeedServer(TimelineService timelineService) {
        this.timelineService = timelineService;
        this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX") // Set gson date format to java date format
                .create();
    }

    @Override
    public void run(String... args) {
        Thread serverThread = new Thread(this::startServer);
        serverThread.setDaemon(true);
        serverThread.start();
    }



    private void startServer() {
        logger.info("Starting Java Socket Feed Server on port {}", port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept(); // Accepts a client
                executorService.submit(() -> handleClient(clientSocket)); // Adds a new client thread into thread pool
            }
        } catch (Exception e) {
            logger.error("Error in Socket Feed Server", e);
        }
    }

    /**
     * Whenever a connection is established between a client and server this method would get called
     * this method works like this:
     * the client would send a UUID through the connection
     * server calls timelineService.getFeed(id) to get timeline for the user
     * wraps it up in json using gson
     * then sends it over to client as string data type.
     * */
    private void handleClient(Socket clientSocket) {
        // Auto closable classes inside parentheses so they close automatically when code inside try scope is finished executing
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine = reader.readLine();
            if (inputLine == null || inputLine.isBlank()) {
                writer.println("Error: Empty User ID");
                return;
            }

            UUID userId;
            try {
                userId = UUID.fromString(inputLine.trim());
            } catch (IllegalArgumentException e) {
                writer.println("Error: Invalid UUID format");
                return;
            }

            List<PostResponse> feed = timelineService.getHomeFeed(userId);
            String jsonFeed = gson.toJson(feed);
            writer.println(jsonFeed);

        } catch (Exception e) {
            logger.error("Error handling socket client connection", e);
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                logger.error("Error closing client socket", e);
            }
        }
    }
}
