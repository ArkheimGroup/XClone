package arkheim.server.application.features.Hashtag.mapper;

import arkheim.server.application.features.Hashtag.dtos.GetHashtagDto;
import arkheim.server.application.models.Hashtag;
import arkheim.server.domain.entities.HashtagEntity;

public final class HashtagMapper {
    public HashtagMapper() {}

    public Hashtag map(HashtagEntity source) {
        return new Hashtag(
                source.getId(),
                source.getName()
        );
    }

    public GetHashtagDto map(Hashtag source) {
        return new GetHashtagDto(
                source.id(),
                source.name()
        );
    }
}
