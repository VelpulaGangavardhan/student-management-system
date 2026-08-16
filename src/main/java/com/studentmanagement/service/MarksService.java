package com.studentmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.studentmanagement.dto.request.MarksRequestDTO;
import com.studentmanagement.dto.response.MarksResponseDTO;
import com.studentmanagement.entity.Exam;
import com.studentmanagement.entity.Marks;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Subject;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.MarksRepository;
import com.studentmanagement.util.GradeCalculator;

@Service
public class MarksService {

    private final MarksRepository marksRepository;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final ExamService examService;

    public MarksService(MarksRepository marksRepository, StudentService studentService,
                         SubjectService subjectService, ExamService examService) {
        this.marksRepository = marksRepository;
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.examService = examService;
    }

    public MarksResponseDTO addMarks(MarksRequestDTO request) {
        if (marksRepository.existsByStudent_IdAndSubject_IdAndExam_Id(
                request.getStudentId(), request.getSubjectId(), request.getExamId())) {
            throw new DuplicateResourceException(
                    "Marks for this student/subject/exam combination already exist - use update instead");
        }
        Marks marks = new Marks();
        applyRequest(marks, request);
        return toResponse(marksRepository.save(marks));
    }

    public MarksResponseDTO updateMarks(Long id, MarksRequestDTO request) {
        Marks existing = getEntityById(id);
        applyRequest(existing, request);
        return toResponse(marksRepository.save(existing));
    }

    public void deleteMarks(Long id) {
        marksRepository.delete(getEntityById(id));
    }

    public Marks getEntityById(Long id) {
        return marksRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Marks", id));
    }

    public MarksResponseDTO getMarksById(Long id) {
        return toResponse(getEntityById(id));
    }

    public List<MarksResponseDTO> getMarksByStudent(Long studentId) {
        return marksRepository.findByStudent_Id(studentId).stream().map(this::toResponse).toList();
    }

    public List<Marks> getMarksEntitiesByStudent(Long studentId) {
        return marksRepository.findByStudent_Id(studentId);
    }

    public List<MarksResponseDTO> getMarksBySubject(Long subjectId) {
        return marksRepository.findBySubject_Id(subjectId).stream().map(this::toResponse).toList();
    }

    public List<MarksResponseDTO> getMarksByExam(Long examId) {
        return marksRepository.findByExam_Id(examId).stream().map(this::toResponse).toList();
    }

    private void applyRequest(Marks marks, MarksRequestDTO request) {
        Student student = studentService.getEntityById(request.getStudentId());
        Subject subject = subjectService.getEntityById(request.getSubjectId());
        Exam exam = examService.getEntityById(request.getExamId());

        if (request.getMarksObtained() > request.getMaximumMarks()) {
            throw new IllegalArgumentException("Marks obtained cannot exceed maximum marks");
        }

        marks.setStudent(student);
        marks.setSubject(subject);
        marks.setExam(exam);
        marks.setMarksObtained(request.getMarksObtained());
        marks.setMaximumMarks(request.getMaximumMarks());
    }

    public MarksResponseDTO toResponse(Marks marks) {
        double percentage = GradeCalculator.percentage(marks.getMarksObtained(), marks.getMaximumMarks());
        return new MarksResponseDTO(
                marks.getId(),
                marks.getStudent().getId(),
                marks.getStudent().getName(),
                marks.getSubject().getId(),
                marks.getSubject().getName(),
                marks.getExam().getId(),
                marks.getExam().getExamName(),
                marks.getMarksObtained(),
                marks.getMaximumMarks(),
                percentage,
                GradeCalculator.grade(percentage),
                GradeCalculator.isPass(percentage));
    }
}
