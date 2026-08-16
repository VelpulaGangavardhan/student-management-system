package com.studentmanagement.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.studentmanagement.dto.response.AttendanceSummaryDTO;
import com.studentmanagement.dto.response.PerformanceResponseDTO;
import com.studentmanagement.dto.response.ResultResponseDTO;
import com.studentmanagement.entity.Student;

/**
 * Rule-based "at-risk student" detection engine (spec section 12).
 * This is explicitly NOT machine learning - it's a transparent set of
 * thresholds anyone can read and explain, which is exactly what's asked for.
 *
 * A student is AT_RISK if any of:
 *   - CGPA < 6.0
 *   - Attendance < 75%
 *   - One or more failed subjects in their latest results
 *
 * Otherwise NEEDS_ATTENTION if CGPA < 7.5 or attendance < 85% (early warning
 * band), else GOOD.
 *
 * The performance score (0-100) is a simple weighted blend: 50% academics
 * (CGPA out of 10, scaled to 100) + 50% attendance percentage - easy to
 * explain in an interview, easy to tune.
 */
@Service
public class PerformanceService {

    private static final double AT_RISK_CGPA = 6.0;
    private static final double WATCH_CGPA = 7.5;
    private static final double WATCH_ATTENDANCE = 85.0;

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final ResultService resultService;

    public PerformanceService(StudentService studentService, AttendanceService attendanceService,
                               ResultService resultService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.resultService = resultService;
    }

    public PerformanceResponseDTO analyze(Long studentId) {
        Student student = studentService.getEntityById(studentId);
        AttendanceSummaryDTO attendance = attendanceService.getSummaryForStudent(studentId);
        ResultResponseDTO result = resultService.getResultForStudent(studentId);

        double cgpa = student.getCgpa() != null ? student.getCgpa() : 0.0;
        double attendancePct = attendance.getAttendancePercentage();
        int failedCount = result.getFailedSubjects().size();

        List<String> reasons = new ArrayList<>();
        String status;

        boolean atRisk = cgpa < AT_RISK_CGPA || attendancePct < AttendanceService.atRiskThreshold() || failedCount > 0;
        boolean needsAttention = cgpa < WATCH_CGPA || attendancePct < WATCH_ATTENDANCE;

        if (atRisk) {
            status = "AT_RISK";
            if (cgpa < AT_RISK_CGPA) reasons.add("CGPA below 6.0");
            if (attendancePct < AttendanceService.atRiskThreshold()) reasons.add("Attendance below 75%");
            if (failedCount > 0) reasons.add(failedCount + " failed subject(s)");
        } else if (needsAttention) {
            status = "NEEDS_ATTENTION";
            if (cgpa < WATCH_CGPA) reasons.add("CGPA trending below 7.5");
            if (attendancePct < WATCH_ATTENDANCE) reasons.add("Attendance trending below 85%");
        } else {
            status = "GOOD";
            reasons.add("Meets CGPA, attendance, and pass-rate expectations");
        }

        double academicScore = (cgpa / 10.0) * 100.0;
        double performanceScore = Math.round(((academicScore * 0.5) + (attendancePct * 0.5)) * 100.0) / 100.0;

        return new PerformanceResponseDTO(
                student.getId(), student.getName(), student.getCgpa(), attendancePct,
                failedCount, performanceScore, status, reasons);
    }
}
