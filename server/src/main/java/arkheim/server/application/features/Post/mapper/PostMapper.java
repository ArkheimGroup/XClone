package arkheim.server.application.features.Post.mapper;

import arkheim.server.application.features.Post.commands.CreatePostCommand;
import arkheim.server.application.features.Post.dtos.GetPostDto;
import arkheim.server.application.features.Post.dtos.PostDetail;
import arkheim.server.application.models.post.CreatePostModel;

public final class PostMapper {
    public PostMapper() {}

    public CreatePostModel map(CreatePostCommand source) {
        return new CreatePostModel(
                source.authorId(),
                source.content(),
                source.mediaUrl(),
                source.parentPostId()
        );
    }

    public GetPostDto map(PostDetail source) {
        return new GetPostDto(
                source.id(),
                source.authorId(),
                source.authorUsername(),
                source.authorName(),
                source.authorPfpUrl(),
                source.content(),
                source.mediaUrls(),
                source.createdAt(),
                source.likeCount(),
                source.repostCount(),
                source.replyCount(),
                source.parentPostId(),
                source.isLikedByMe(),
                source.isRepostedByMe()
        );
    }
}
