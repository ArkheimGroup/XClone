package arkheim.server.infrastructure.api;

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

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Catches NoSuchElementException (maps to 404 Not Found)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // HTTP 404
                .body(Map.of("error", ex.getMessage()));
    }

    // Catches SecurityException (maps to 403 Forbidden)
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        logger.warn("Access forbidden: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // HTTP 403
                .body(Map.of("error", ex.getMessage()));
    }

    // Catches IllegalStateException (maps to 409 Conflict)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        logger.warn("Conflict error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // HTTP 409
                .body(Map.of("error", ex.getMessage()));
    }

    // Catches IllegalArgumentException (maps to 400 Bad Request)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // HTTP 400
                .body(Map.of("error", ex.getMessage()));
    }

    // Catches JSON parsing errors (malformed input)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.warn("Malformed JSON request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // HTTP 400
                .body(Map.of("error", "Malformed JSON request body"));
    }

    // Catches parameter/path variable type mismatch errors
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' should be of type '%s'", ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        logger.warn("Type mismatch error: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // HTTP 400
                .body(Map.of("error", message));
    }

    // Catches unsupported HTTP methods
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        logger.warn("HTTP Method not supported: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED) // HTTP 405
                .body(Map.of("error", ex.getMessage()));
    }

    // Catches static resource or endpoint 404 errors
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(NoResourceFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // HTTP 404
                .body(Map.of("error", "Resource not found: " + ex.getResourcePath()));
    }

    // Catches generic unhandled RuntimeExceptions (returns HTTP 500 without leaking details)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleGeneralRuntime(RuntimeException ex) {
        logger.error("Unhandled internal runtime error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // HTTP 500
                .body(Map.of("error", "An unexpected error occurred. Please try again later."));
    }

    // Catches any general unhandled exceptions (fallback to prevent stack trace leaks)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        logger.error("Unhandled internal server error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // HTTP 500
                .body(Map.of("error", "An unexpected error occurred. Please try again later."));
    }
}
