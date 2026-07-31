package arkheim.server.domain.exception;

public class BadArgumentException extends BaseApplicationException {
    public BadArgumentException(ResultCode code, String message) {
        super(code, message);
    }
}
