package com.studentmanagement.dto.response;

/**
 * Deliberately minimal: no password, no password hash, no JWT. The
 * registration flow is register -> success message -> redirect to login ->
 * login issues the JWT, exactly as the design calls for.
 */
public class RegistrationResponseDTO {

    private boolean success;
    private String message;

    public RegistrationResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
