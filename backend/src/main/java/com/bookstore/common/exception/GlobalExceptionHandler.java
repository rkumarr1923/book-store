package com.bookstore.common.exception;

import com.bookstore.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralised exception handler.
 * Converts all application exceptions into the standard {@link ApiResponse} error envelope.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Bean validation (@Valid / @Validated) ─────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ApiResponse.FieldError> fieldErrors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new ApiResponse.FieldError(fe.getField(), fe.getDefaultMessage()));
        }

        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "Validation failed", fieldErrors, request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        List<ApiResponse.FieldError> fieldErrors = new ArrayList<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            String field = cv.getPropertyPath().toString();
            fieldErrors.add(new ApiResponse.FieldError(field, cv.getMessage()));
        }

        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "Constraint violation", fieldErrors, request.getRequestURI());
    }

    // ── Application-specific exceptions ───────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "NOT_FOUND",
                ex.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                ex.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                ex.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(
            DuplicateResourceException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "CONFLICT",
                ex.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(
            BusinessRuleException ex, HttpServletRequest request) {
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY",
                ex.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                ex.getMessage(), null, request.getRequestURI());
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                null, request.getRequestURI());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ResponseEntity<ApiResponse<Void>> buildError(
            HttpStatus status,
            String errorCode,
            String message,
            List<ApiResponse.FieldError> errors,
            String path) {

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .success(false)
                .status(status.value())
                .error(errorCode)
                .message(message)
                .errors(errors)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
