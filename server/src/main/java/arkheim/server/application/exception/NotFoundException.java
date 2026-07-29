package arkheim.server.application.exception;

public class NotFoundException extends BaseApplicationException {
    public NotFoundException(ErrorCode code, String message) {
        super(code, message);
    }
}
