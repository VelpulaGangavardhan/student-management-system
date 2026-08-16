package com.studentmanagement.dto.response;

public class StudentDashboardDTO {

    private StudentResponseDTO profile;
    private AttendanceSummaryDTO attendance;
    private ResultResponseDTO latestResult;
    private PerformanceResponseDTO performance;

    public StudentDashboardDTO(StudentResponseDTO profile, AttendanceSummaryDTO attendance,
                                ResultResponseDTO latestResult, PerformanceResponseDTO performance) {
        this.profile = profile;
        this.attendance = attendance;
        this.latestResult = latestResult;
        this.performance = performance;
    }

    public StudentResponseDTO getProfile() { return profile; }
    public AttendanceSummaryDTO getAttendance() { return attendance; }
    public ResultResponseDTO getLatestResult() { return latestResult; }
    public PerformanceResponseDTO getPerformance() { return performance; }
}
