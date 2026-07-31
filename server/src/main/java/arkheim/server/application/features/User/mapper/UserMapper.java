package arkheim.server.application.features.User.mapper;

import arkheim.server.application.features.User.commands.UpdateUserProfileCommand;
import arkheim.server.application.features.User.dtos.GetUserDto;
import arkheim.server.application.features.User.dtos.GetUserProfileDto;
import arkheim.server.application.models.user.MinimalUser;
import arkheim.server.application.models.user.User;
import arkheim.server.domain.entities.UserEntity;

public final class UserMapper {
    public UserMapper() {}

    public User map(UserEntity source) {
        return new User(
                source.getId(),
                source.getUsername(),
                source.getName(),
                source.getEmail(),
                source.getBiography(),
                source.getDateOfBirth(),
                source.getPfpUrl(),
                source.getFollowerCount(),
                source.getFollowingCount(),
                source.getCreatedAt(),
                source.getPinnedPostId(),
                source.getBannerUrl(),
                source.isVerified()
        );
    }

    public GetUserProfileDto map(User source) {
        return new GetUserProfileDto(
                source.id(),
                source.username(),
                source.name(),
                source.email(),
                source.biography(),
                source.dateOfBirth(),
                source.pfpUrl(),
                source.followerCount(),
                source.followingCount(),
                source.createdAt(),
                source.pinnedPostId()
        );
    }

    public User map(UpdateUserProfileCommand source) {
        return new User(
                source.userId(),
                null,
                source.name(),
                null,
                source.biography(),
                source.dateOfBirth(),
                source.pfpUrl(),
                null,
                null,
                null,
                null,
                source.bannerUrl(),
                source.isVerified()
        );
    }

    public MinimalUser mapToMinimalUser(UserEntity source) {
        return new MinimalUser(
                source.getId(),
                source.getUsername(),
                source.getEmail(),
                source.getName(),
                source.getPfpUrl(),
                source.getBannerUrl(),
                source.isVerified()
        );
    }

    public GetUserDto map(MinimalUser source) {
        return new GetUserDto(
                source.id(),
                source.username(),
                source.email(),
                source.name(),
                source.pfpUrl(),
                source.bannerUrl(),
                source.isVerified()
        );
    }
}
