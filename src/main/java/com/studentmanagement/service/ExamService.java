package com.studentmanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.request.ExamRequestDTO;
import com.studentmanagement.dto.response.ExamResponseDTO;
import com.studentmanagement.entity.Exam;
import com.studentmanagement.entity.ExamType;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.ExamRepository;

@Service
public class ExamService {

    private final ExamRepository examRepository;

    public ExamService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public ExamResponseDTO createExam(ExamRequestDTO request) {
        Exam exam = new Exam();
        applyRequest(exam, request);
        return toResponse(examRepository.save(exam));
    }

    public Exam getEntityById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Exam", id));
    }

    public ExamResponseDTO getExamById(Long id) {
        return toResponse(getEntityById(id));
    }

    public Page<ExamResponseDTO> getAllExams(Pageable pageable) {
        return examRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<ExamResponseDTO> filterBySemester(Integer semester, Pageable pageable) {
        return examRepository.findBySemester(semester, pageable).map(this::toResponse);
    }

    public ExamResponseDTO updateExam(Long id, ExamRequestDTO request) {
        Exam existing = getEntityById(id);
        applyRequest(existing, request);
        return toResponse(examRepository.save(existing));
    }

    public void deleteExam(Long id) {
        examRepository.delete(getEntityById(id));
    }

    private void applyRequest(Exam exam, ExamRequestDTO request) {
        exam.setExamName(request.getExamName());
        try {
            exam.setExamType(ExamType.valueOf(request.getExamType().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Exam type must be one of INTERNAL, MIDTERM, FINAL, PRACTICAL");
        }
        exam.setDate(request.getDate());
        exam.setSemester(request.getSemester());
        exam.setAcademicYear(request.getAcademicYear());
    }

    private ExamResponseDTO toResponse(Exam exam) {
        return new ExamResponseDTO(
                exam.getId(),
                exam.getExamName(),
                exam.getExamType().name(),
                exam.getDate(),
                exam.getSemester(),
                exam.getAcademicYear());
    }
}
