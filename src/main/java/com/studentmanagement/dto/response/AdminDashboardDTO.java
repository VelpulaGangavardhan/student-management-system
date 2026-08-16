package com.studentmanagement.dto.response;

public class AdminDashboardDTO {

    private long totalStudents;
    private long totalTeachers;
    private long totalDepartments;
    private long totalSubjects;
    private long totalUsers;
    private long atRiskStudents;
    private double averageCgpa;
    private double averageAttendancePercentage;

    public AdminDashboardDTO(long totalStudents, long totalTeachers, long totalDepartments, long totalSubjects,
                              long totalUsers, long atRiskStudents, double averageCgpa,
                              double averageAttendancePercentage) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalDepartments = totalDepartments;
        this.totalSubjects = totalSubjects;
        this.totalUsers = totalUsers;
        this.atRiskStudents = atRiskStudents;
        this.averageCgpa = averageCgpa;
        this.averageAttendancePercentage = averageAttendancePercentage;
    }

    public long getTotalStudents() { return totalStudents; }
    public long getTotalTeachers() { return totalTeachers; }
    public long getTotalDepartments() { return totalDepartments; }
    public long getTotalSubjects() { return totalSubjects; }
    public long getTotalUsers() { return totalUsers; }
    public long getAtRiskStudents() { return atRiskStudents; }
    public double getAverageCgpa() { return averageCgpa; }
    public double getAverageAttendancePercentage() { return averageAttendancePercentage; }
}
