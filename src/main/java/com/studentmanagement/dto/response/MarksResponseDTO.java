package com.studentmanagement.dto.response;

public class MarksResponseDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private Long examId;
    private String examName;
    private Double marksObtained;
    private Double maximumMarks;
    private Double percentage;
    private String grade;
    private boolean passed;

    public MarksResponseDTO(Long id, Long studentId, String studentName, Long subjectId, String subjectName,
                             Long examId, String examName, Double marksObtained, Double maximumMarks,
                             Double percentage, String grade, boolean passed) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.examId = examId;
        this.examName = examName;
        this.marksObtained = marksObtained;
        this.maximumMarks = maximumMarks;
        this.percentage = percentage;
        this.grade = grade;
        this.passed = passed;
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public Long getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public Long getExamId() { return examId; }
    public String getExamName() { return examName; }
    public Double getMarksObtained() { return marksObtained; }
    public Double getMaximumMarks() { return maximumMarks; }
    public Double getPercentage() { return percentage; }
    public String getGrade() { return grade; }
    public boolean isPassed() { return passed; }
}
