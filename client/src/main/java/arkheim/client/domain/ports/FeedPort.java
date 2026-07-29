package arkheim.client.domain.ports;

import arkheim.client.domain.ports.dtos.PostDto;

import java.util.List;
import java.util.UUID;

public interface FeedPort {

    /**
     * Fetches the home feed for the given user over the TCP socket.
     */
    List<PostDto> getHomeFeed(UUID userId);
}
