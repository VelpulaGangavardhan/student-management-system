package com.studentmanagement.exception;

/**
 * Thrown by service-layer checks such as "a student may only view their own
 * record" - distinct from Spring Security's own 401/403 handling, which
 * covers "not logged in" / "wrong role" at the filter-chain level.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
