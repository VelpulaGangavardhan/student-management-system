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

import com.studentmanagement.dto.request.ExamRequestDTO;
import com.studentmanagement.dto.response.ExamResponseDTO;
import com.studentmanagement.service.ExamService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping
    public ResponseEntity<ExamResponseDTO> create(@Valid @RequestBody ExamRequestDTO request) {
        return new ResponseEntity<>(examService.createExam(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ExamResponseDTO>> getAll(
            @RequestParam(required = false) Integer semester,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        if (semester != null) {
            return ResponseEntity.ok(examService.filterBySemester(semester, pageable));
        }
        return ResponseEntity.ok(examService.getAllExams(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<ExamResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ExamRequestDTO request) {
        return ResponseEntity.ok(examService.updateExam(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }
}
