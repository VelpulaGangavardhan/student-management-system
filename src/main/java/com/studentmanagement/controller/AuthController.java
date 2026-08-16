package com.studentmanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.LoginRequestDTO;
import com.studentmanagement.dto.request.StudentRegistrationRequestDTO;
import com.studentmanagement.dto.response.LoginResponseDTO;
import com.studentmanagement.dto.response.RegistrationResponseDTO;
import com.studentmanagement.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Public registration is intentionally STUDENT-only. The DTO has no role
    // field, so there is no request shape that can create an ADMIN or
    // TEACHER account here - those are created exclusively by an admin via
    // POST /api/users or POST /api/teachers (both ROLE_ADMIN-protected).
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDTO> register(@Valid @RequestBody StudentRegistrationRequestDTO request) {
        RegistrationResponseDTO response = authService.registerStudent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
