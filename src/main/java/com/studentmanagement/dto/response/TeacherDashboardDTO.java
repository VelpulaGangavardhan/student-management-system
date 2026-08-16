package com.studentmanagement.dto.response;

import java.util.List;

public class TeacherDashboardDTO {

    private String teacherName;
    private List<SubjectResponseDTO> assignedSubjects;
    private long totalStudentsTaught;
    private long pendingMarksEntries;

    public TeacherDashboardDTO(String teacherName, List<SubjectResponseDTO> assignedSubjects,
                                long totalStudentsTaught, long pendingMarksEntries) {
        this.teacherName = teacherName;
        this.assignedSubjects = assignedSubjects;
        this.totalStudentsTaught = totalStudentsTaught;
        this.pendingMarksEntries = pendingMarksEntries;
    }

    public String getTeacherName() { return teacherName; }
    public List<SubjectResponseDTO> getAssignedSubjects() { return assignedSubjects; }
    public long getTotalStudentsTaught() { return totalStudentsTaught; }
    public long getPendingMarksEntries() { return pendingMarksEntries; }
}
