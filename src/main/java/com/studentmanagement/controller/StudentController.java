package com.studentmanagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.request.StudentRequestDTO;
import com.studentmanagement.dto.response.StudentResponseDTO;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Student.StudentStatus;
import com.studentmanagement.exception.UnauthorizedAccessException;
import com.studentmanagement.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StudentResponseDTO> addStudent(@Valid @RequestBody StudentRequestDTO request) {
        return new ResponseEntity<>(studentService.createStudent(request), HttpStatus.CREATED);
    }

    // A student may only fetch their own record; admin/teacher can fetch any.
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> getStudentById(@PathVariable Long id, Authentication auth) {
        StudentResponseDTO student = studentService.getStudentById(id);
        assertSelfOrStaff(auth, student.getId());
        return ResponseEntity.ok(student);
    }

    // Convenience endpoint for the logged-in student's own dashboard/profile page.
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me")
    public ResponseEntity<StudentResponseDTO> getMyProfile(Authentication auth) {
        Student student = studentService.getEntityByUsername(auth.getName());
        return ResponseEntity.ok(studentService.toResponse(student));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping
    public ResponseEntity<Page<StudentResponseDTO>> getAllStudents(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(studentService.getAllStudents(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDTO request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/search")
    public ResponseEntity<Page<StudentResponseDTO>> searchStudents(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(studentService.searchStudents(keyword, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/filter")
    public ResponseEntity<Page<StudentResponseDTO>> filterStudents(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) StudentStatus status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        if (departmentId != null) {
            return ResponseEntity.ok(studentService.filterByDepartment(departmentId, pageable));
        }
        if (year != null) {
            return ResponseEntity.ok(studentService.filterByYear(year, pageable));
        }
        if (status != null) {
            return ResponseEntity.ok(studentService.filterByStatus(status, pageable));
        }
        return ResponseEntity.ok(studentService.getAllStudents(pageable));
    }

    /**
     * Enforces "a student can view their own data only" - the rule the
     * project spec calls out explicitly (no changing the ID in the URL to
     * see someone else's record). ADMIN and TEACHER bypass this check.
     */
    private void assertSelfOrStaff(Authentication auth, Long studentId) {
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));
        if (isStaff) {
            return;
        }
        Student own = studentService.getEntityByUsername(auth.getName());
        if (!own.getId().equals(studentId)) {
            throw new UnauthorizedAccessException("You may only access your own student record");
        }
    }
}
