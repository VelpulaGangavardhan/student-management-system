package com.studentmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.dto.response.AdminDashboardDTO;
import com.studentmanagement.dto.response.StudentDashboardDTO;
import com.studentmanagement.dto.response.TeacherDashboardDTO;
import com.studentmanagement.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardDTO> admin() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/teacher")
    public ResponseEntity<TeacherDashboardDTO> teacher(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getTeacherDashboard(auth.getName()));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student")
    public ResponseEntity<StudentDashboardDTO> student(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getStudentDashboard(auth.getName()));
    }
}
