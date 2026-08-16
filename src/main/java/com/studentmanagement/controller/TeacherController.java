package com.studentmanagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.TeacherRequestDTO;
import com.studentmanagement.dto.response.TeacherResponseDTO;
import com.studentmanagement.service.TeacherService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TeacherResponseDTO> create(@Valid @RequestBody TeacherRequestDTO request) {
        return new ResponseEntity<>(teacherService.createTeacher(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/me")
    public ResponseEntity<TeacherResponseDTO> getMyProfile(org.springframework.security.core.Authentication auth) {
        return ResponseEntity.ok(teacherService.toResponse(teacherService.getEntityByUsername(auth.getName())));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping
    public ResponseEntity<Page<TeacherResponseDTO>> getAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(teacherService.getAllTeachers(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/search")
    public ResponseEntity<Page<TeacherResponseDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(teacherService.searchTeachers(keyword, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/filter")
    public ResponseEntity<Page<TeacherResponseDTO>> filterByDepartment(
            @RequestParam Long departmentId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(teacherService.filterByDepartment(departmentId, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody TeacherRequestDTO request) {
        return ResponseEntity.ok(teacherService.updateTeacher(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}
