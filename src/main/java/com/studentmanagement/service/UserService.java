package com.studentmanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.request.RegisterRequestDTO;
import com.studentmanagement.dto.response.UserResponseDTO;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO createUser(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Role must be one of ADMIN, TEACHER, STUDENT");
        }
        User user = new User(request.getUsername(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()), role);
        return toResponse(userRepository.save(user));
    }

    public UserResponseDTO getUserById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<UserResponseDTO> searchUsers(String keyword, Pageable pageable) {
        return userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::toResponse);
    }

    public UserResponseDTO setEnabled(Long id, boolean enabled) {
        User user = findOrThrow(id);
        user.setEnabled(enabled);
        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.delete(findOrThrow(id));
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    private UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().name(), user.isEnabled(), user.getCreatedAt());
    }
}
