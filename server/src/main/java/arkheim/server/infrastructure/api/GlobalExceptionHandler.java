package arkheim.server.infrastructure.api;

import arkheim.server.application.dtos.ApiResponse;
import arkheim.server.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// If ApiResponse is passed with no ResultCode, frontend will avoid showing the message to a normal user
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ApiResponse> UserResponseHelper(BaseApplicationException ex, HttpStatus status) {
        ApiResponse response = ApiResponse.failure(ex.getCode(), ex.getUserMessage());

        return ResponseEntity
                .status(status)
                .body(response);
    }

    private ResponseEntity<ApiResponse> responseHelper(Exception ex, HttpStatus status) {
        ApiResponse response = ApiResponse.failure(null, ex.getMessage());

        return ResponseEntity
                .status(status)
                .body(response);
    }

    private ResponseEntity<ApiResponse> responseHelper(Exception ex, HttpStatus status, String message) {
        ApiResponse response = ApiResponse.failure(null, message);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Catches NotFoundException, equivalent to NoSuchElementException (maps to 404 Not Found)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(NotFoundException ex) {
        logger.warn("Not found: {}", ex.getMessage());

        return UserResponseHelper(ex, HttpStatus.NOT_FOUND); // HTTP 404
    }

    // Catches ForbiddenException, equivalent to SecurityException (maps to 403 Forbidden)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse> handleForbidden(ForbiddenException ex) {
        logger.warn("Access forbidden: {}", ex.getMessage());

        return UserResponseHelper(ex, HttpStatus.FORBIDDEN); // HTTP 403
    }

    // Catches ConflictException, equivalent to IllegalStateException (maps to 409 Conflict)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflict(ConflictException ex) {
        logger.warn("Conflict error: {}", ex.getMessage());

        return UserResponseHelper(ex, HttpStatus.CONFLICT); // HTTP 409
    }

    // Catches BadArgumentException, equivalent to IllegalArgumentException (maps to 400 Bad Request)
    @ExceptionHandler(BadArgumentException.class)
    public ResponseEntity<ApiResponse> handleBadRequest(BadArgumentException ex) {
        logger.warn("Bad request: {}", ex.getMessage());

        return UserResponseHelper(ex, HttpStatus.BAD_REQUEST); // HTTP 400
    }

    // Catches HttpMessageNotReadableException (malformed input)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.warn("Malformed JSON request: {}", ex.getMessage());

        return responseHelper(ex, HttpStatus.BAD_REQUEST); // HTTP 400
    }

    // Catches parameter/path variable type mismatch exceptions
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' should be of type '%s'", ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        logger.warn("Type mismatch error: {}", message);

        return responseHelper(ex, HttpStatus.BAD_REQUEST, message); // HTTP 400
    }

    // Catches unsupported HTTP methods exception
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        logger.warn("HTTP Method not supported: {}", ex.getMessage());

        return responseHelper(ex, HttpStatus.METHOD_NOT_ALLOWED); // HTTP 405
    }

    // Catches ResourceNotFoundException, equivalent to static resource or endpoint 404 errors
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(NoResourceFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());

        return responseHelper(
                ex,
                HttpStatus.NOT_FOUND, // HTTP 404
                "Resource not found: " + ex.getResourcePath()
        );
    }

    // Catches business customized exceptions, equivalent to other BaseApplicationException subclasses (returns HTTP 400)
    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<ApiResponse> handleBaseException(BaseApplicationException ex) {
        logger.warn("Business related exception: {}", ex.getMessage());

        return UserResponseHelper(ex, HttpStatus.BAD_REQUEST); // HTTP 400
    }

    // Catches generic unhandled RuntimeExceptions (returns HTTP 500 without leaking details)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleGeneralRuntime(RuntimeException ex) {
        logger.error("Unhandled internal runtime error occurred", ex);

        return responseHelper(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR, // HTTP 500
                "An unexpected error occurred. Please try again later."
        );
    }

    // Catches any general unhandled exceptions (fallback to prevent stack trace leaks)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        logger.error("Unhandled internal server error occurred", ex);

        return responseHelper(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR, // HTTP 500
                "An unexpected error occurred. Please try again later."
        );
    }
}
