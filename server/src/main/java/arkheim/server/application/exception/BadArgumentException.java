package arkheim.server.application.exception;

public class BadArgumentException extends BaseApplicationException {
    public BadArgumentException(ErrorCode code, String message) {
        super(code, message);
    }
}
