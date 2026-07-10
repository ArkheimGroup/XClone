package arkheim.server.infrastructure.config;

import arkheim.server.application.ports.PasswordEncoderPort;
import arkheim.server.application.services.*;
import arkheim.server.domain.repository.*;
import arkheim.server.infrastructure.utils.BCryptPasswordEncoderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoderPort passwordEncoderPort() {
        return new BCryptPasswordEncoderPort();
    }

    @Bean
    public AuthService authService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        return new AuthService(userRepository, passwordEncoderPort);
    }

    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserService(userRepository);
    }

    @Bean
    public FollowUserService followUserService(UserRepository userRepository, FollowRepository followRepository) {
        return new FollowUserService(userRepository, followRepository);
    }

    @Bean
    public HashtagService hashtagService(
            HashtagRepository hashtagRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            MediaRepository mediaRepository,
            LikeRepository likeRepository
    ) {
        return new HashtagService(hashtagRepository, postRepository, userRepository, mediaRepository, likeRepository);
    }

    @Bean
    public MediaService mediaService(MediaRepository mediaRepository) {
        return new MediaService(mediaRepository);
    }

    @Bean
    public PostService postService(
            PostRepository postRepository,
            MediaRepository mediaRepository,
            UserRepository userRepository,
            LikeRepository likeRepository
    ) {
        return new PostService(postRepository, mediaRepository, userRepository, likeRepository);
    }

    @Bean
    public TimelineService timelineService(
            PostRepository postRepository,
            UserRepository userRepository,
            LikeRepository likeRepository,
            MediaRepository mediaRepository
    ) {
        return new TimelineService(postRepository, userRepository, likeRepository, mediaRepository);
    }
}
