package arkheim.server.domain.exception;

public class NotFoundException extends BaseApplicationException {
    public NotFoundException(ResultCode code, String message) {
        super(code, message);
    }
}
