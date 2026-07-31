package arkheim.server.domain.exception;

public abstract class BaseApplicationException extends RuntimeException {
    private final ResultCode code;
    private final String message;

    public BaseApplicationException(ResultCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResultCode getCode() {
        return code;
    }


    public String getUserMessage() {
        return message;
    }
}

