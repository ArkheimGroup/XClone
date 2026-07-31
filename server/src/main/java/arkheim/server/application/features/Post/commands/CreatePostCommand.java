package arkheim.server.application.features.Post.commands;

import java.util.UUID;

public record CreatePostCommand(
        UUID authorId,
        String content,
        String mediaUrl,
        UUID parentPostId
) { }
