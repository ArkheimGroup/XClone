package arkheim.server.application.exception;

public enum ErrorCode {
    // --- User ---
    USER_NOT_FOUND,
    USER_NOT_AUTHORIZED_TO_DELETE_POST,
    USERNAME_ALREADY_EXISTS,
    EMAIL_ALREADY_EXISTS,
    INVALID_EMAIL_OR_PASSWORD,

    // --- Post ---
    POST_NOT_FOUND,
    AUTHOR_NOT_FOUND,
    PARENT_POST_NOT_FOUND,

    // --- media ---
    MEDIA_NOT_FOUND,
}
