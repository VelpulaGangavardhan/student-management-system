package com.studentmanagement.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExamRequestDTO {

    @NotBlank(message = "Exam name is required")
    private String examName;

    @NotBlank(message = "Exam type is required")
    private String examType;

    @NotNull(message = "Exam date is required")
    private LocalDate date;

    @Min(1) @Max(8)
    private Integer semester;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
