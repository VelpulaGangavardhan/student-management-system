package com.studentmanagement.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String path;
    private Map<String, String> validationErrors;

    public ErrorResponse(int status, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String message, String path, Map<String, String> validationErrors) {
        this(status, message, path);
        this.validationErrors = validationErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
