package com.studentmanagement.dto.response;

import java.util.List;

public class ResultResponseDTO {

    private Long studentId;
    private String studentName;
    private double totalMarksObtained;
    private double totalMaximumMarks;
    private double overallPercentage;
    private String overallGrade;
    private double gpa;
    private List<MarksResponseDTO> subjectResults;
    private List<String> failedSubjects;
    private boolean overallPass;

    public ResultResponseDTO(Long studentId, String studentName, double totalMarksObtained,
                              double totalMaximumMarks, double overallPercentage, String overallGrade,
                              double gpa, List<MarksResponseDTO> subjectResults, List<String> failedSubjects,
                              boolean overallPass) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalMarksObtained = totalMarksObtained;
        this.totalMaximumMarks = totalMaximumMarks;
        this.overallPercentage = overallPercentage;
        this.overallGrade = overallGrade;
        this.gpa = gpa;
        this.subjectResults = subjectResults;
        this.failedSubjects = failedSubjects;
        this.overallPass = overallPass;
    }

    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public double getTotalMarksObtained() { return totalMarksObtained; }
    public double getTotalMaximumMarks() { return totalMaximumMarks; }
    public double getOverallPercentage() { return overallPercentage; }
    public String getOverallGrade() { return overallGrade; }
    public double getGpa() { return gpa; }
    public List<MarksResponseDTO> getSubjectResults() { return subjectResults; }
    public List<String> getFailedSubjects() { return failedSubjects; }
    public boolean isOverallPass() { return overallPass; }
}
