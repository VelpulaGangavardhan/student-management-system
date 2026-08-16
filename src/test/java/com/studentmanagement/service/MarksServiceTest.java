package com.studentmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studentmanagement.dto.request.MarksRequestDTO;
import com.studentmanagement.dto.response.MarksResponseDTO;
import com.studentmanagement.entity.Exam;
import com.studentmanagement.entity.ExamType;
import com.studentmanagement.entity.Marks;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Subject;
import com.studentmanagement.repository.MarksRepository;

/**
 * Covers marks calculation (percentage / grade / pass-fail) - the "business
 * logic in the service layer" requirement from the project spec.
 */
@ExtendWith(MockitoExtension.class)
class MarksServiceTest {

    @Mock
    private MarksRepository marksRepository;

    @Mock
    private StudentService studentService;

    @Mock
    private SubjectService subjectService;

    @Mock
    private ExamService examService;

    @InjectMocks
    private MarksService marksService;

    private Student student;
    private Subject subject;
    private Exam exam;
    private MarksRequestDTO request;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setName("Test Student");

        subject = new Subject();
        subject.setId(1L);
        subject.setName("Data Structures");

        exam = new Exam();
        exam.setId(1L);
        exam.setExamName("Midterm");
        exam.setExamType(ExamType.MIDTERM);

        request = new MarksRequestDTO();
        request.setStudentId(1L);
        request.setSubjectId(1L);
        request.setExamId(1L);
        request.setMarksObtained(42.0);
        request.setMaximumMarks(50.0);
    }

    @Test
    void addMarksCalculatesPercentageGradeAndPassFail() {
        when(marksRepository.existsByStudent_IdAndSubject_IdAndExam_Id(1L, 1L, 1L)).thenReturn(false);
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(subjectService.getEntityById(1L)).thenReturn(subject);
        when(examService.getEntityById(1L)).thenReturn(exam);
        when(marksRepository.save(any(Marks.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarksResponseDTO response = marksService.addMarks(request);

        // 42/50 = 84%
        assertEquals(84.0, response.getPercentage());
        assertEquals("A", response.getGrade());
        assertTrue(response.isPassed());
    }

    @Test
    void failingMarksAreCorrectlyFlagged() {
        request.setMarksObtained(20.0); // 20/50 = 40%
        when(marksRepository.existsByStudent_IdAndSubject_IdAndExam_Id(1L, 1L, 1L)).thenReturn(false);
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(subjectService.getEntityById(1L)).thenReturn(subject);
        when(examService.getEntityById(1L)).thenReturn(exam);
        when(marksRepository.save(any(Marks.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarksResponseDTO response = marksService.addMarks(request);

        assertEquals(40.0, response.getPercentage());
        assertEquals("F", response.getGrade());
        assertTrue(!response.isPassed());
    }

    @Test
    void addMarksRejectsMarksAboveMaximum() {
        request.setMarksObtained(60.0); // exceeds maximum of 50

        assertTrue(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            when(marksRepository.existsByStudent_IdAndSubject_IdAndExam_Id(1L, 1L, 1L)).thenReturn(false);
            when(studentService.getEntityById(1L)).thenReturn(student);
            when(subjectService.getEntityById(1L)).thenReturn(subject);
            when(examService.getEntityById(1L)).thenReturn(exam);
            marksService.addMarks(request);
        }).getMessage().contains("cannot exceed"));
    }
}
