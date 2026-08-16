package com.studentmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.MarksRequestDTO;
import com.studentmanagement.dto.response.MarksResponseDTO;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.UnauthorizedAccessException;
import com.studentmanagement.service.MarksService;
import com.studentmanagement.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/marks")
public class MarksController {

    private final MarksService marksService;
    private final StudentService studentService;

    public MarksController(MarksService marksService, StudentService studentService) {
        this.marksService = marksService;
        this.studentService = studentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping
    public ResponseEntity<MarksResponseDTO> add(@Valid @RequestBody MarksRequestDTO request) {
        return new ResponseEntity<>(marksService.addMarks(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<MarksResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MarksRequestDTO request) {
        return ResponseEntity.ok(marksService.updateMarks(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        marksService.deleteMarks(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<MarksResponseDTO>> getByStudent(@PathVariable Long studentId, Authentication auth) {
        assertSelfOrStaff(auth, studentId);
        return ResponseEntity.ok(marksService.getMarksByStudent(studentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<MarksResponseDTO>> getBySubject(@PathVariable Long subjectId) {
        return ResponseEntity.ok(marksService.getMarksBySubject(subjectId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<MarksResponseDTO>> getByExam(@PathVariable Long examId) {
        return ResponseEntity.ok(marksService.getMarksByExam(examId));
    }

    private void assertSelfOrStaff(Authentication auth, Long studentId) {
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));
        if (isStaff) {
            return;
        }
        Student own = studentService.getEntityByUsername(auth.getName());
        if (!own.getId().equals(studentId)) {
            throw new UnauthorizedAccessException("You may only view your own marks");
        }
    }
}
