package com.studentmanagement.dto.response;

import java.time.LocalDate;

public class ExamResponseDTO {

    private Long id;
    private String examName;
    private String examType;
    private LocalDate date;
    private Integer semester;
    private String academicYear;

    public ExamResponseDTO(Long id, String examName, String examType, LocalDate date, Integer semester, String academicYear) {
        this.id = id;
        this.examName = examName;
        this.examType = examType;
        this.date = date;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    public Long getId() { return id; }
    public String getExamName() { return examName; }
    public String getExamType() { return examType; }
    public LocalDate getDate() { return date; }
    public Integer getSemester() { return semester; }
    public String getAcademicYear() { return academicYear; }
}
