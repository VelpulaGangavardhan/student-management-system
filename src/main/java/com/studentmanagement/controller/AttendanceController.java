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

import com.studentmanagement.dto.request.AttendanceRequestDTO;
import com.studentmanagement.dto.response.AttendanceResponseDTO;
import com.studentmanagement.dto.response.AttendanceSummaryDTO;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.UnauthorizedAccessException;
import com.studentmanagement.service.AttendanceService;
import com.studentmanagement.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final StudentService studentService;

    public AttendanceController(AttendanceService attendanceService, StudentService studentService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping
    public ResponseEntity<AttendanceResponseDTO> record(@Valid @RequestBody AttendanceRequestDTO request) {
        return new ResponseEntity<>(attendanceService.recordAttendance(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody AttendanceRequestDTO request) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceResponseDTO>> getByStudent(@PathVariable Long studentId, Authentication auth) {
        assertSelfOrStaff(auth, studentId);
        return ResponseEntity.ok(attendanceService.getByStudent(studentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<AttendanceSummaryDTO> getSummary(@PathVariable Long studentId, Authentication auth) {
        assertSelfOrStaff(auth, studentId);
        return ResponseEntity.ok(attendanceService.getSummaryForStudent(studentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<AttendanceResponseDTO>> getBySubject(@PathVariable Long subjectId) {
        return ResponseEntity.ok(attendanceService.getBySubject(subjectId));
    }

    private void assertSelfOrStaff(Authentication auth, Long studentId) {
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));
        if (isStaff) {
            return;
        }
        Student own = studentService.getEntityByUsername(auth.getName());
        if (!own.getId().equals(studentId)) {
            throw new UnauthorizedAccessException("You may only view your own attendance");
        }
    }
}
