package com.studentmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.studentmanagement.dto.request.LoginRequestDTO;
import com.studentmanagement.dto.request.RegisterRequestDTO;
import com.studentmanagement.dto.response.LoginResponseDTO;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.security.JwtUtil;

/**
 * Covers the project's "Login" and authentication-related testing
 * requirement. AuthenticationManager itself is mocked - we're testing
 * AuthService's own logic (duplicate checks, token issuance), not Spring
 * Security's internals.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", "admin@college.edu", "hashed-password", Role.ADMIN);
        adminUser.setId(1L);
    }

    @Test
    void login_returnsTokenOnValidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(jwtUtil.generateToken("admin", "ADMIN")).thenReturn("fake-jwt-token");

        LoginResponseDTO response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void login_propagatesBadCredentials() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("admin");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void register_rejectsDuplicateUsername() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("admin");
        request.setEmail("new@college.edu");
        request.setPassword("Passw0rd!");
        request.setRole("STUDENT");

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void register_createsUserWithEncodedPassword() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("newstudent");
        request.setEmail("newstudent@college.edu");
        request.setPassword("Passw0rd!");
        request.setRole("STUDENT");

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("newstudent@college.edu")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rd!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User created = authService.register(request);

        assertThat(created.getPassword()).isEqualTo("encoded-password");
        assertThat(created.getRole()).isEqualTo(Role.STUDENT);
    }
}
