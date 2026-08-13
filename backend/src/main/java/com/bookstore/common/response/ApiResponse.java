package com.bookstore.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Standard API response envelope used for all endpoints.
 *
 * <pre>
 * Success:  { success: true,  data: {...}, message: "..." }
 * Error:    { success: false, status: 400, error: "BAD_REQUEST", message: "...", errors: [...] }
 * </pre>
 *
 * @param <T> the type of the {@code data} payload
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

    // Error-only fields
    private final Integer status;
    private final String error;
    private final List<FieldError> errors;
    private final String path;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    // ── Static factory methods ────────────────────────────────────────────────

    /** Wrap a successful data payload. */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /** Wrap a successful data payload with a custom message. */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /** Acknowledgement response with no data body. */
    public static <T> ApiResponse<T> ok(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    // ── Nested types ──────────────────────────────────────────────────────────

    /**
     * Represents a single field-level validation error within the {@code errors} array.
     */
    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
