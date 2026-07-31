package arkheim.server.domain.exception;

public class ConflictException extends BaseApplicationException {
    public ConflictException(ResultCode code, String message) {
        super(code, message);
    }
}
