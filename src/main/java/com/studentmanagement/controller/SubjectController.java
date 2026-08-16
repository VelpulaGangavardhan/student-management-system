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

import com.studentmanagement.dto.request.SubjectRequestDTO;
import com.studentmanagement.dto.response.SubjectResponseDTO;
import com.studentmanagement.service.SubjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SubjectResponseDTO> create(@Valid @RequestBody SubjectRequestDTO request) {
        return new ResponseEntity<>(subjectService.createSubject(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubjectById(id));
    }

    @GetMapping
    public ResponseEntity<Page<SubjectResponseDTO>> getAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(subjectService.getAllSubjects(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SubjectResponseDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(subjectService.searchSubjects(keyword, pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<SubjectResponseDTO>> filter(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer semester,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        if (departmentId != null) {
            return ResponseEntity.ok(subjectService.filterByDepartment(departmentId, pageable));
        }
        if (semester != null) {
            return ResponseEntity.ok(subjectService.filterBySemester(semester, pageable));
        }
        return ResponseEntity.ok(subjectService.getAllSubjects(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody SubjectRequestDTO request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}
