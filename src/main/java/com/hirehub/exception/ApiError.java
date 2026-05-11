package com.hirehub.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error payload returned by {@link GlobalExceptionHandler}.
 */
@Data
@Builder
public class ApiError {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    /** Populated only for validation errors */
    private Map<String, String> fieldErrors;
}
