package com.uo289390.toco.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;

/**
 * Centralized exception handler for all REST controllers.
 *
 * This class applies the {@code @RestControllerAdvice} pattern to translate the
 * exceptions thrown across the application into consistent HTTP responses,
 * removing the need for scattered {@code try/catch} blocks inside controllers.
 *
 * The mapping preserves the HTTP status codes that were already produced by the
 * controllers (e.g. {@link BadCredentialsException} -> 401 and
 * {@link IllegalArgumentException} -> 400) and simply unifies the error payload.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-06-20
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles authentication failures (invalid credentials).
     *
     * @param ex the thrown exception
     * @return a 401 Unauthorized response
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Handles {@link AccessDeniedException}, thrown by method security ({@code @PreAuthorize})
     * when an authenticated user acts on data that is not theirs.
     *
     * Without this handler the generic {@link Exception} handler would turn it into a 500.
     *
     * @param ex the access denied exception
     * @return a 403 Forbidden response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied");
    }

    /**
     * Handles invalid arguments (e.g. unresolved related entities by id).
     *
     * @param ex the thrown exception
     * @return a 400 Bad Request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles missing entities.
     *
     * @param ex the thrown exception
     * @return a 404 Not Found response
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles bean validation errors raised by {@code @Valid} request bodies.
     *
     * @param ex the thrown exception
     * @return a 400 Bad Request response including the field validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation failed");
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        body.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles request bodies that cannot be read: missing body, malformed JSON or
     * values that cannot be converted to the target field type.
     *
     * @param ex the thrown exception
     * @return a 400 Bad Request response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        logger.warn("Unreadable request body: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed or missing request body");
    }

    /**
     * Handles path variables or request parameters with a wrong type
     * (e.g. {@code /api/chords/abc} where a numeric id is expected).
     *
     * @param ex the thrown exception
     * @return a 400 Bad Request response naming the offending parameter
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'");
    }

    /**
     * Fallback handler for any unexpected exception.
     *
     * Spring MVC exceptions (unknown route, wrong HTTP method, missing parameter...)
     * implement {@link ErrorResponse} and already carry their own status, so they
     * keep it instead of ending in a 500.
     *
     * @param ex the thrown exception
     * @return the status of the Spring MVC exception, or 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            logger.warn("{}: {}", status, ex.getMessage());
            return buildResponse(status, ex.getMessage());
        }
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    /**
     * Builds a consistent error response body.
     *
     * @param status  the HTTP status to return
     * @param message the human-readable error message
     * @return the response entity wrapping the error body
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseBody(status, message));
    }

    /**
     * Creates the base error payload shared by all handlers.
     *
     * @param status  the HTTP status to return
     * @param message the human-readable error message
     * @return a mutable map with the common error fields
     */
    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}

