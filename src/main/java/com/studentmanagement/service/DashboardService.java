package com.studentmanagement.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.response.AdminDashboardDTO;
import com.studentmanagement.dto.response.AttendanceSummaryDTO;
import com.studentmanagement.dto.response.PerformanceResponseDTO;
import com.studentmanagement.dto.response.ResultResponseDTO;
import com.studentmanagement.dto.response.StudentDashboardDTO;
import com.studentmanagement.dto.response.StudentResponseDTO;
import com.studentmanagement.dto.response.SubjectResponseDTO;
import com.studentmanagement.dto.response.TeacherDashboardDTO;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Subject;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.MarksRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.SubjectRepository;
import com.studentmanagement.repository.TeacherRepository;
import com.studentmanagement.repository.UserRepository;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final MarksRepository marksRepository;
    private final PerformanceService performanceService;
    private final AttendanceService attendanceService;
    private final ResultService resultService;
    private final StudentService studentService;
    private final SubjectService subjectService;

    public DashboardService(StudentRepository studentRepository, TeacherRepository teacherRepository,
                             DepartmentRepository departmentRepository, SubjectRepository subjectRepository,
                             UserRepository userRepository, MarksRepository marksRepository,
                             PerformanceService performanceService, AttendanceService attendanceService,
                             ResultService resultService, StudentService studentService,
                             SubjectService subjectService) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.marksRepository = marksRepository;
        this.performanceService = performanceService;
        this.attendanceService = attendanceService;
        this.resultService = resultService;
        this.studentService = studentService;
        this.subjectService = subjectService;
    }

    /**
     * Note: computing at-risk count by running the rule-based analysis for
     * every student is fine at college-project scale (hundreds of students).
     * For a much larger dataset this would move to a scheduled batch job
     * that pre-computes and caches the status instead of doing it on every
     * dashboard load.
     */
    public AdminDashboardDTO getAdminDashboard() {
        List<Student> allStudents = studentRepository.findAll();

        long atRisk = allStudents.stream()
                .filter(s -> "AT_RISK".equals(performanceService.analyze(s.getId()).getStatus()))
                .count();

        double avgAttendance = allStudents.isEmpty() ? 0.0 : allStudents.stream()
                .mapToDouble(s -> attendanceService.getSummaryForStudent(s.getId()).getAttendancePercentage())
                .average()
                .orElse(0.0);

        Double avgCgpa = studentRepository.averageCgpa();

        return new AdminDashboardDTO(
                studentRepository.count(),
                teacherRepository.count(),
                departmentRepository.count(),
                subjectRepository.count(),
                userRepository.count(),
                atRisk,
                avgCgpa != null ? Math.round(avgCgpa * 100.0) / 100.0 : 0.0,
                Math.round(avgAttendance * 100.0) / 100.0);
    }

    public TeacherDashboardDTO getTeacherDashboard(String username) {
        Teacher teacher = teacherRepository.findByUser_Username(username)
                .orElseThrow(() -> new com.studentmanagement.exception.ResourceNotFoundException(
                        "No teacher profile is linked to this account"));

        List<Subject> subjects = subjectService.getSubjectsByTeacher(teacher.getId());
        List<SubjectResponseDTO> subjectDtos = subjects.stream().map(subjectService::toResponse).toList();

        long studentsTaught = subjects.stream()
                .flatMap(sub -> marksRepository.findBySubject_Id(sub.getId()).stream())
                .map(m -> m.getStudent().getId())
                .distinct()
                .count();

        // "Pending marks" here means subject/student pairs enrolled in the
        // department+semester with no marks entry yet for this subject -
        // approximated by counting students in the subject's department at
        // that semester who have zero marks rows for this subject.
        long pendingMarks = subjects.stream()
                .mapToLong(sub -> {
                    long deptStudents = studentRepository.findByDepartment_Id(sub.getDepartment().getId(),
                            Pageable.unpaged()).getTotalElements();
                    long graded = marksRepository.findBySubject_Id(sub.getId()).stream()
                            .map(m -> m.getStudent().getId()).distinct().count();
                    return Math.max(0, deptStudents - graded);
                })
                .sum();

        return new TeacherDashboardDTO(teacher.getName(), subjectDtos, studentsTaught, pendingMarks);
    }

    public StudentDashboardDTO getStudentDashboard(String username) {
        Student student = studentService.getEntityByUsername(username);
        StudentResponseDTO profile = studentService.toResponse(student);
        AttendanceSummaryDTO attendance = attendanceService.getSummaryForStudent(student.getId());
        ResultResponseDTO result = resultService.getResultForStudent(student.getId());
        PerformanceResponseDTO performance = performanceService.analyze(student.getId());

        return new StudentDashboardDTO(profile, attendance, result, performance);
    }
}
