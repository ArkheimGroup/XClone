package arkheim.server.domain.exception;

public class ForbiddenException extends BaseApplicationException {
    public ForbiddenException(ResultCode code, String message) {
        super(code, message);
    }
}
