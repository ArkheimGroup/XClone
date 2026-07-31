package arkheim.server.application.features.Media.mapper;

import arkheim.server.application.features.Media.dtos.GetMediaDto;
import arkheim.server.application.models.Media;
import arkheim.server.domain.entities.MediaEntity;

public final class MediaMapper {
    public MediaMapper() {}

    public Media map(MediaEntity source) {
        return new Media(
                source.getId(),
                source.getUrl(),
                source.getWidth(),
                source.getHeight(),
                source.getFileSize(),
                source.getUploadedBy(),
                source.getCreatedAt()
        );
    }

    public GetMediaDto map(Media source) {
        return new GetMediaDto(
                source.id(),
                source.url(),
                source.width(),
                source.height(),
                source.fileSize(),
                source.uploadedBy(),
                source.createdAt()
        );
    }
}
