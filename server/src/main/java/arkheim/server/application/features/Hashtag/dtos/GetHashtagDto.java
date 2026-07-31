package arkheim.server.application.features.Hashtag.dtos;

import java.util.UUID;

public record GetHashtagDto(
        UUID id,
        String name
) { }
