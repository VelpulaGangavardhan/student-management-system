package com.studentmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studentmanagement.dto.request.StudentRequestDTO;
import com.studentmanagement.dto.response.StudentResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.StudentNotFoundException;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;

/**
 * Covers the core "Student creation / retrieval / update / deletion"
 * requirement from the project spec's testing section, using Mockito so
 * this runs without a real database or Spring context.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentService studentService;

    private StudentRequestDTO request;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");
        department.setCode("CSE");

        request = new StudentRequestDTO();
        request.setStudentId("S100");
        request.setName("Test Student");
        request.setEmail("test.student@example.com");
        request.setPhone("9876543210");
        request.setDateOfBirth(LocalDate.of(2003, 1, 1));
        request.setDepartmentId(1L);
        request.setYear(2);
        request.setSemester(3);
        request.setCgpa(8.0);
        request.setAdmissionDate(LocalDate.of(2022, 8, 1));
        request.setStatus("ACTIVE");
    }

    @Test
    void createStudent_savesWhenEmailAndIdAreUnique() {
        when(studentRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(studentRepository.existsByStudentId(request.getStudentId())).thenReturn(false);
        when(departmentService.getEntityById(1L)).thenReturn(department);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        StudentResponseDTO result = studentService.createStudent(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStudentId()).isEqualTo("S100");
        assertThat(result.getDepartmentName()).isEqualTo("Computer Science");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createStudent_rejectsDuplicateEmail() {
        when(studentRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(request));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_rejectsDuplicateStudentId() {
        when(studentRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(studentRepository.existsByStudentId(request.getStudentId())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(request));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void getStudentById_throwsWhenMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> studentService.getStudentById(99L));
    }

    @Test
    void getStudentById_returnsMappedDto() {
        Student student = buildExistingStudent();
        when(studentRepository.findById(5L)).thenReturn(Optional.of(student));

        StudentResponseDTO result = studentService.getStudentById(5L);

        assertThat(result.getName()).isEqualTo("Existing Student");
        assertThat(result.getEmail()).isEqualTo("existing@example.com");
    }

    @Test
    void updateStudent_appliesChangesAndSaves() {
        Student existing = buildExistingStudent();
        when(studentRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(departmentService.getEntityById(1L)).thenReturn(department);
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        request.setName("Updated Name");
        StudentResponseDTO result = studentService.updateStudent(5L, request);

        assertThat(result.getName()).isEqualTo("Updated Name");
        verify(studentRepository).save(existing);
    }

    @Test
    void deleteStudent_removesExistingRecord() {
        Student existing = buildExistingStudent();
        when(studentRepository.findById(5L)).thenReturn(Optional.of(existing));

        studentService.deleteStudent(5L);

        verify(studentRepository).delete(existing);
    }

    @Test
    void deleteStudent_throwsWhenMissing() {
        when(studentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> studentService.deleteStudent(404L));
        verify(studentRepository, never()).delete(any());
    }

    private Student buildExistingStudent() {
        Student student = new Student();
        student.setId(5L);
        student.setStudentId("S005");
        student.setName("Existing Student");
        student.setEmail("existing@example.com");
        student.setPhone("9999999999");
        student.setDepartment(department);
        student.setYear(1);
        student.setSemester(1);
        student.setCgpa(7.0);
        student.setAdmissionDate(LocalDate.of(2021, 1, 1));
        student.setStatus(Student.StudentStatus.ACTIVE);
        return student;
    }
}
