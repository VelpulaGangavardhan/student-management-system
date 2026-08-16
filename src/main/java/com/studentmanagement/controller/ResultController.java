package com.studentmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.response.PerformanceResponseDTO;
import com.studentmanagement.dto.response.ResultResponseDTO;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.UnauthorizedAccessException;
import com.studentmanagement.service.PerformanceService;
import com.studentmanagement.service.ResultService;
import com.studentmanagement.service.StudentService;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;
    private final PerformanceService performanceService;
    private final StudentService studentService;

    public ResultController(ResultService resultService, PerformanceService performanceService,
                             StudentService studentService) {
        this.resultService = resultService;
        this.performanceService = performanceService;
        this.studentService = studentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResultResponseDTO> getResult(@PathVariable Long studentId, Authentication auth) {
        assertSelfOrStaff(auth, studentId);
        return ResponseEntity.ok(resultService.getResultForStudent(studentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/student/{studentId}/performance")
    public ResponseEntity<PerformanceResponseDTO> getPerformance(@PathVariable Long studentId, Authentication auth) {
        assertSelfOrStaff(auth, studentId);
        return ResponseEntity.ok(performanceService.analyze(studentId));
    }

    private void assertSelfOrStaff(Authentication auth, Long studentId) {
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));
        if (isStaff) {
            return;
        }
        Student own = studentService.getEntityByUsername(auth.getName());
        if (!own.getId().equals(studentId)) {
            throw new UnauthorizedAccessException("You may only view your own results");
        }
    }
}
