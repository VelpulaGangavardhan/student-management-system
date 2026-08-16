package com.studentmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studentmanagement.dto.response.AttendanceSummaryDTO;
import com.studentmanagement.dto.response.PerformanceResponseDTO;
import com.studentmanagement.dto.response.ResultResponseDTO;
import com.studentmanagement.entity.Student;

/**
 * Covers the rule-based at-risk classification described in spec section 12:
 * AT_RISK if CGPA < 6.0 OR attendance < 75% OR any failed subject; GOOD
 * otherwise (once above the NEEDS_ATTENTION watch bands too).
 */
@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    private StudentService studentService;
    @Mock
    private AttendanceService attendanceService;
    @Mock
    private ResultService resultService;

    @InjectMocks
    private PerformanceService performanceService;

    @Test
    void analyze_flagsAtRiskForLowCgpa() {
        Student student = studentWithCgpa(5.5);
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(attendanceService.getSummaryForStudent(1L))
                .thenReturn(new AttendanceSummaryDTO(1L, "Test", 10, 9, 1, 90.0, false));
        when(resultService.getResultForStudent(1L))
                .thenReturn(result(Collections.emptyList()));

        PerformanceResponseDTO result = performanceService.analyze(1L);

        assertEquals("AT_RISK", result.getStatus());
    }

    @Test
    void analyze_flagsAtRiskForLowAttendance() {
        Student student = studentWithCgpa(9.0);
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(attendanceService.getSummaryForStudent(1L))
                .thenReturn(new AttendanceSummaryDTO(1L, "Test", 10, 6, 4, 60.0, true));
        when(resultService.getResultForStudent(1L))
                .thenReturn(result(Collections.emptyList()));

        PerformanceResponseDTO result = performanceService.analyze(1L);

        assertEquals("AT_RISK", result.getStatus());
    }

    @Test
    void analyze_flagsAtRiskForFailedSubjects() {
        Student student = studentWithCgpa(9.0);
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(attendanceService.getSummaryForStudent(1L))
                .thenReturn(new AttendanceSummaryDTO(1L, "Test", 10, 9, 1, 90.0, false));
        when(resultService.getResultForStudent(1L))
                .thenReturn(result(List.of("Data Structures")));

        PerformanceResponseDTO result = performanceService.analyze(1L);

        assertEquals("AT_RISK", result.getStatus());
    }

    @Test
    void analyze_reportsGoodWhenAllThresholdsMet() {
        Student student = studentWithCgpa(9.0);
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(attendanceService.getSummaryForStudent(1L))
                .thenReturn(new AttendanceSummaryDTO(1L, "Test", 10, 10, 0, 100.0, false));
        when(resultService.getResultForStudent(1L))
                .thenReturn(result(Collections.emptyList()));

        PerformanceResponseDTO result = performanceService.analyze(1L);

        assertEquals("GOOD", result.getStatus());
    }

    private Student studentWithCgpa(double cgpa) {
        Student student = new Student();
        student.setId(1L);
        student.setName("Test Student");
        student.setCgpa(cgpa);
        return student;
    }

    private ResultResponseDTO result(List<String> failedSubjects) {
        return new ResultResponseDTO(1L, "Test Student", 0, 0, 0, "F", 0,
                Collections.emptyList(), failedSubjects, failedSubjects.isEmpty());
    }
}
