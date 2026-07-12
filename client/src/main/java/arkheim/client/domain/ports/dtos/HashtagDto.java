package arkheim.client.domain.ports.dtos;

import java.util.UUID;

/**
 * Client-side DTO mirroring the server's Hashtag} entity as returned by HashtagController}.
 */
public record HashtagDto(
        UUID id,
        String name
) {}
