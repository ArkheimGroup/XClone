package arkheim.server.infrastructure.api;

import arkheim.server.application.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError (
        ErrorCode code,
        String message,
        HttpStatus status
) { }
