package arkheim.server.application.exception;

public class ConflictException extends BaseApplicationException {
    public ConflictException(ErrorCode code, String message) {
        super(code, message);
    }
}
