package com.studentmanagement.dto.response;

import java.util.List;

public class PerformanceResponseDTO {

    private Long studentId;
    private String studentName;
    private Double cgpa;
    private double attendancePercentage;
    private int failedSubjectCount;
    private double performanceScore;
    private String status; // GOOD / NEEDS_ATTENTION / AT_RISK
    private List<String> reasons;

    public PerformanceResponseDTO(Long studentId, String studentName, Double cgpa, double attendancePercentage,
                                   int failedSubjectCount, double performanceScore, String status,
                                   List<String> reasons) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.cgpa = cgpa;
        this.attendancePercentage = attendancePercentage;
        this.failedSubjectCount = failedSubjectCount;
        this.performanceScore = performanceScore;
        this.status = status;
        this.reasons = reasons;
    }

    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public Double getCgpa() { return cgpa; }
    public double getAttendancePercentage() { return attendancePercentage; }
    public int getFailedSubjectCount() { return failedSubjectCount; }
    public double getPerformanceScore() { return performanceScore; }
    public String getStatus() { return status; }
    public List<String> getReasons() { return reasons; }
}
