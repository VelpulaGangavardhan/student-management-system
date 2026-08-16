package com.studentmanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class MarksRequestDTO {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotNull(message = "Marks obtained is required")
    @DecimalMin(value = "0.0", message = "Marks obtained cannot be negative")
    private Double marksObtained;

    @NotNull(message = "Maximum marks is required")
    @DecimalMin(value = "1.0", message = "Maximum marks must be at least 1")
    private Double maximumMarks;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }
    public Double getMaximumMarks() { return maximumMarks; }
    public void setMaximumMarks(Double maximumMarks) { this.maximumMarks = maximumMarks; }
}
