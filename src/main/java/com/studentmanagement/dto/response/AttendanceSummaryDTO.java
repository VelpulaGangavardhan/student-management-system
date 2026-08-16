package com.studentmanagement.dto.response;

public class AttendanceSummaryDTO {

    private Long studentId;
    private String studentName;
    private long totalClasses;
    private long presentClasses;
    private long absentClasses;
    private double attendancePercentage;
    private boolean atRisk;

    public AttendanceSummaryDTO(Long studentId, String studentName, long totalClasses, long presentClasses,
                                 long absentClasses, double attendancePercentage, boolean atRisk) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalClasses = totalClasses;
        this.presentClasses = presentClasses;
        this.absentClasses = absentClasses;
        this.attendancePercentage = attendancePercentage;
        this.atRisk = atRisk;
    }

    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public long getTotalClasses() { return totalClasses; }
    public long getPresentClasses() { return presentClasses; }
    public long getAbsentClasses() { return absentClasses; }
    public double getAttendancePercentage() { return attendancePercentage; }
    public boolean isAtRisk() { return atRisk; }
}
