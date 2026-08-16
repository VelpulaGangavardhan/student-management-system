package com.studentmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studentmanagement.dto.response.AttendanceSummaryDTO;
import com.studentmanagement.entity.Attendance;
import com.studentmanagement.entity.AttendanceStatus;
import com.studentmanagement.entity.Student;
import com.studentmanagement.repository.AttendanceRepository;

/**
 * Covers the project's "Attendance calculation" testing requirement:
 * total/present/absent counts, the percentage, and the AT_RISK flag below
 * the 75% threshold described in the spec.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private StudentService studentService;
    @Mock private SubjectService subjectService;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void summary_flagsAtRiskBelowSeventyFivePercent() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Rahul");

        // 6 present, 4 absent out of 10 => 60%, below the 75% threshold.
        List<Attendance> records = buildRecords(student, 6, 4);

        when(studentService.getEntityById(1L)).thenReturn(student);
        when(attendanceRepository.findByStudent_Id(1L)).thenReturn(records);

        AttendanceSummaryDTO summary = attendanceService.getSummaryForStudent(1L);

        assertThat(summary.getTotalClasses()).isEqualTo(10);
        assertThat(summary.getPresentClasses()).isEqualTo(6);
        assertThat(summary.getAbsentClasses()).isEqualTo(4);
        assertThat(summary.getAttendancePercentage()).isEqualTo(60.0);
        assertThat(summary.isAtRisk()).isTrue();
    }

    @Test
    void summary_notAtRiskAtOrAboveSeventyFivePercent() {
        Student student = new Student();
        student.setId(2L);
        student.setName("Priya");

        // 8 present, 2 absent out of 10 => 80%.
        List<Attendance> records = buildRecords(student, 8, 2);

        when(studentService.getEntityById(2L)).thenReturn(student);
        when(attendanceRepository.findByStudent_Id(2L)).thenReturn(records);

        AttendanceSummaryDTO summary = attendanceService.getSummaryForStudent(2L);

        assertThat(summary.getAttendancePercentage()).isEqualTo(80.0);
        assertThat(summary.isAtRisk()).isFalse();
    }

    @Test
    void summary_handlesNoRecordsWithoutDivideByZero() {
        Student student = new Student();
        student.setId(3L);
        student.setName("No Records");

        when(studentService.getEntityById(3L)).thenReturn(student);
        when(attendanceRepository.findByStudent_Id(3L)).thenReturn(List.of());

        AttendanceSummaryDTO summary = attendanceService.getSummaryForStudent(3L);

        assertThat(summary.getTotalClasses()).isZero();
        assertThat(summary.getAttendancePercentage()).isEqualTo(0.0);
    }

    private List<Attendance> buildRecords(Student student, int present, int absent) {
        List<Attendance> records = new java.util.ArrayList<>();
        for (int i = 0; i < present; i++) {
            records.add(attendanceOf(student, AttendanceStatus.PRESENT, i));
        }
        for (int i = 0; i < absent; i++) {
            records.add(attendanceOf(student, AttendanceStatus.ABSENT, present + i));
        }
        return records;
    }

    private Attendance attendanceOf(Student student, AttendanceStatus status, int dayOffset) {
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setDate(LocalDate.now().minusDays(dayOffset));
        attendance.setStatus(status);
        return attendance;
    }
}
