package com.studentmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.studentmanagement.dto.response.MarksResponseDTO;
import com.studentmanagement.dto.response.ResultResponseDTO;
import com.studentmanagement.entity.Marks;
import com.studentmanagement.entity.Student;
import com.studentmanagement.util.GradeCalculator;

/**
 * Aggregates a student's Marks records into one overall result: total marks,
 * percentage, grade, a rough GPA, and which subjects (if any) were failed.
 * Business logic lives here rather than in the controller, per the project's
 * layering rule.
 */
@Service
public class ResultService {

    private final MarksService marksService;
    private final StudentService studentService;

    public ResultService(MarksService marksService, StudentService studentService) {
        this.marksService = marksService;
        this.studentService = studentService;
    }

    public ResultResponseDTO getResultForStudent(Long studentId) {
        Student student = studentService.getEntityById(studentId);
        List<Marks> marksList = marksService.getMarksEntitiesByStudent(studentId);

        double totalObtained = marksList.stream().mapToDouble(Marks::getMarksObtained).sum();
        double totalMax = marksList.stream().mapToDouble(Marks::getMaximumMarks).sum();
        double overallPercentage = totalMax == 0 ? 0.0 : GradeCalculator.percentage(totalObtained, totalMax);

        List<MarksResponseDTO> subjectResults = marksList.stream().map(marksService::toResponse).toList();

        List<String> failedSubjects = subjectResults.stream()
                .filter(m -> !m.isPassed())
                .map(MarksResponseDTO::getSubjectName)
                .toList();

        return new ResultResponseDTO(
                student.getId(),
                student.getName(),
                totalObtained,
                totalMax,
                overallPercentage,
                GradeCalculator.grade(overallPercentage),
                GradeCalculator.gpaEquivalent(overallPercentage),
                subjectResults,
                failedSubjects,
                failedSubjects.isEmpty() && !marksList.isEmpty());
    }
}
