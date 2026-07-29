package arkheim.server.application.exception;

public abstract class BaseApplicationException extends RuntimeException {
    private final ErrorCode code;
    private final String message;

    public BaseApplicationException(ErrorCode code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ErrorCode getCode() {
        return code;
    }


    public String getUserMessage() {
        return message;
    }
}

