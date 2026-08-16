package com.studentmanagement.config;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.entity.Attendance;
import com.studentmanagement.entity.AttendanceStatus;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Exam;
import com.studentmanagement.entity.ExamType;
import com.studentmanagement.entity.Marks;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Subject;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.repository.AttendanceRepository;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.ExamRepository;
import com.studentmanagement.repository.MarksRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.SubjectRepository;
import com.studentmanagement.repository.TeacherRepository;
import com.studentmanagement.repository.UserRepository;

/**
 * Seeds a small, self-consistent set of development data - one admin,
 * one teacher, one student, one department/subject/exam, plus a handful of
 * marks and attendance rows - so the app is usable immediately after first
 * run without any manual Postman setup.
 *
 * Guarded by app.sample-data.enabled (default true) and, more importantly,
 * by checking userRepository.count() == 0 first, so it never runs again -
 * and never touches data - once real users exist. Restarting the app does
 * NOT re-insert or duplicate anything.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final boolean enabled;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;
    private final MarksRepository marksRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            @Value("${app.sample-data.enabled:true}") boolean enabled,
            UserRepository userRepository, DepartmentRepository departmentRepository,
            TeacherRepository teacherRepository, StudentRepository studentRepository,
            SubjectRepository subjectRepository, ExamRepository examRepository,
            MarksRepository marksRepository, AttendanceRepository attendanceRepository,
            PasswordEncoder passwordEncoder) {
        this.enabled = enabled;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.examRepository = examRepository;
        this.marksRepository = marksRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || userRepository.count() > 0) {
            return; // Already seeded (or real data exists) - never re-run.
        }

        User admin = userRepository.save(
                new User("admin", "admin@college.edu", passwordEncoder.encode("Admin@123"), Role.ADMIN));
        User teacherUser = userRepository.save(
                new User("teacher1", "teacher1@college.edu", passwordEncoder.encode("Teacher@123"), Role.TEACHER));
        User studentUser = userRepository.save(
                new User("student1", "student1@college.edu", passwordEncoder.encode("Student@123"), Role.STUDENT));

        Department cse = new Department();
        cse.setName("Computer Science and Engineering");
        cse.setCode("CSE");
        cse.setDescription("Department of Computer Science and Engineering");
        cse = departmentRepository.save(cse);

        Teacher teacher = new Teacher();
        teacher.setTeacherId("T001");
        teacher.setName("Dr. Ananya Rao");
        teacher.setEmail("teacher1@college.edu");
        teacher.setPhone("9876543210");
        teacher.setQualification("Ph.D in Computer Science");
        teacher.setSpecialization("Data Structures & Algorithms");
        teacher.setDepartment(cse);
        teacher.setUser(teacherUser);
        teacher = teacherRepository.save(teacher);

        Subject dsa = new Subject();
        dsa.setCode("CS201");
        dsa.setName("Data Structures & Algorithms");
        dsa.setCredits(4);
        dsa.setSemester(3);
        dsa.setDepartment(cse);
        dsa.setTeacher(teacher);
        dsa = subjectRepository.save(dsa);

        Subject dbms = new Subject();
        dbms.setCode("CS202");
        dbms.setName("Database Management Systems");
        dbms.setCredits(4);
        dbms.setSemester(3);
        dbms.setDepartment(cse);
        dbms.setTeacher(teacher);
        dbms = subjectRepository.save(dbms);

        Student student = new Student();
        student.setStudentId("S001");
        student.setName("Rahul Sharma");
        student.setEmail("student1@college.edu");
        student.setPhone("9876500000");
        student.setDateOfBirth(LocalDate.of(2004, 5, 14));
        student.setGender(Student.Gender.MALE);
        student.setAddress("Bengaluru, Karnataka");
        student.setDepartment(cse);
        student.setYear(2);
        student.setSemester(3);
        student.setCgpa(8.4);
        student.setAdmissionDate(LocalDate.of(2023, 8, 1));
        student.setStatus(Student.StudentStatus.ACTIVE);
        student.setUser(studentUser);
        student = studentRepository.save(student);

        Exam midterm = new Exam();
        midterm.setExamName("Semester 3 Midterm");
        midterm.setExamType(ExamType.MIDTERM);
        midterm.setDate(LocalDate.now().minusMonths(1));
        midterm.setSemester(3);
        midterm.setAcademicYear("2025-2026");
        midterm = examRepository.save(midterm);

        Marks dsaMarks = new Marks();
        dsaMarks.setStudent(student);
        dsaMarks.setSubject(dsa);
        dsaMarks.setExam(midterm);
        dsaMarks.setMarksObtained(78.0);
        dsaMarks.setMaximumMarks(100.0);
        marksRepository.save(dsaMarks);

        Marks dbmsMarks = new Marks();
        dbmsMarks.setStudent(student);
        dbmsMarks.setSubject(dbms);
        dbmsMarks.setExam(midterm);
        dbmsMarks.setMarksObtained(85.0);
        dbmsMarks.setMaximumMarks(100.0);
        marksRepository.save(dbmsMarks);

        // A handful of attendance rows: 8 present, 2 absent per subject (80%).
        for (int i = 0; i < 8; i++) {
            saveAttendance(student, dsa, LocalDate.now().minusDays(20 - i), AttendanceStatus.PRESENT);
        }
        for (int i = 0; i < 2; i++) {
            saveAttendance(student, dsa, LocalDate.now().minusDays(12 - i), AttendanceStatus.ABSENT);
        }

        System.out.println("=================================================================");
        System.out.println(" Sample data created. Development login credentials:");
        System.out.println("   ADMIN    -> username: admin     password: Admin@123");
        System.out.println("   TEACHER  -> username: teacher1  password: Teacher@123");
        System.out.println("   STUDENT  -> username: student1  password: Student@123");
        System.out.println("=================================================================");
    }

    private void saveAttendance(Student student, Subject subject, LocalDate date, AttendanceStatus status) {
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSubject(subject);
        attendance.setDate(date);
        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }
}
