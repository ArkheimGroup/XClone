package arkheim.client.domain.dtos.Hashtag.response;

import java.util.UUID;

public record HashtagDto(
        UUID id,
        String name
) { }
