package arkheim.server.application.models.post;

import java.util.UUID;

public record CreatePostModel(

        UUID authorId,
        String content,
        String mediaUrl,
        UUID parentPostId
) { }
