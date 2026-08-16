package com.studentmanagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.RegisterRequestDTO;
import com.studentmanagement.dto.response.UserResponseDTO;
import com.studentmanagement.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody RegisterRequestDTO request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAll(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseDTO>> search(
            @RequestParam String keyword, @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(keyword, pageable));
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserResponseDTO> setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        return ResponseEntity.ok(userService.setEnabled(id, enabled));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
