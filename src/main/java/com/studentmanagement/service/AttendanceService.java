package com.studentmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.studentmanagement.dto.request.AttendanceRequestDTO;
import com.studentmanagement.dto.response.AttendanceResponseDTO;
import com.studentmanagement.dto.response.AttendanceSummaryDTO;
import com.studentmanagement.entity.Attendance;
import com.studentmanagement.entity.AttendanceStatus;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Subject;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.AttendanceRepository;

@Service
public class AttendanceService {

    // Attendance below this threshold is flagged AT_RISK - see spec section 11/12.
    private static final double AT_RISK_THRESHOLD = 75.0;

    private final AttendanceRepository attendanceRepository;
    private final StudentService studentService;
    private final SubjectService subjectService;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentService studentService,
                              SubjectService subjectService) {
        this.attendanceRepository = attendanceRepository;
        this.studentService = studentService;
        this.subjectService = subjectService;
    }

    public AttendanceResponseDTO recordAttendance(AttendanceRequestDTO request) {
        if (attendanceRepository.existsByStudent_IdAndSubject_IdAndDate(
                request.getStudentId(), request.getSubjectId(), request.getDate())) {
            throw new DuplicateResourceException(
                    "Attendance for this student/subject/date already recorded - use update instead");
        }
        Attendance attendance = new Attendance();
        applyRequest(attendance, request);
        return toResponse(attendanceRepository.save(attendance));
    }

    public AttendanceResponseDTO updateAttendance(Long id, AttendanceRequestDTO request) {
        Attendance existing = getEntityById(id);
        applyRequest(existing, request);
        return toResponse(attendanceRepository.save(existing));
    }

    public void deleteAttendance(Long id) {
        attendanceRepository.delete(getEntityById(id));
    }

    public Attendance getEntityById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Attendance record", id));
    }

    public List<AttendanceResponseDTO> getByStudent(Long studentId) {
        return attendanceRepository.findByStudent_Id(studentId).stream().map(this::toResponse).toList();
    }

    public List<AttendanceResponseDTO> getBySubject(Long subjectId) {
        return attendanceRepository.findBySubject_Id(subjectId).stream().map(this::toResponse).toList();
    }

    /**
     * Calculates total/present/absent classes and the attendance percentage
     * for one student, and flags AT_RISK if it falls below the threshold.
     * This is the same rule reused by the performance-analysis engine.
     */
    public AttendanceSummaryDTO getSummaryForStudent(Long studentId) {
        Student student = studentService.getEntityById(studentId);
        List<Attendance> records = attendanceRepository.findByStudent_Id(studentId);

        long total = records.size();
        long present = records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = total - present;
        double percentage = total == 0 ? 0.0 : Math.round((present * 10000.0) / total) / 100.0;

        return new AttendanceSummaryDTO(
                student.getId(), student.getName(), total, present, absent, percentage,
                percentage < AT_RISK_THRESHOLD);
    }

    public static double atRiskThreshold() {
        return AT_RISK_THRESHOLD;
    }

    private void applyRequest(Attendance attendance, AttendanceRequestDTO request) {
        Student student = studentService.getEntityById(request.getStudentId());
        Subject subject = subjectService.getEntityById(request.getSubjectId());
        attendance.setStudent(student);
        attendance.setSubject(subject);
        attendance.setDate(request.getDate());
        try {
            attendance.setStatus(AttendanceStatus.valueOf(request.getStatus().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Status must be PRESENT or ABSENT");
        }
    }

    private AttendanceResponseDTO toResponse(Attendance attendance) {
        return new AttendanceResponseDTO(
                attendance.getId(),
                attendance.getStudent().getId(),
                attendance.getStudent().getName(),
                attendance.getSubject().getId(),
                attendance.getSubject().getName(),
                attendance.getDate(),
                attendance.getStatus().name());
    }
}
