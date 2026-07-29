package arkheim.server.application.exception;

public class ForbiddenException extends BaseApplicationException {
    public ForbiddenException(ErrorCode code, String message) {
        super(code, message);
    }
}
