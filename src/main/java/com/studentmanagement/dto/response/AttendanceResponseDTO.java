package com.studentmanagement.dto.response;

import java.time.LocalDate;

public class AttendanceResponseDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private LocalDate date;
    private String status;

    public AttendanceResponseDTO(Long id, Long studentId, String studentName, Long subjectId, String subjectName,
                                  LocalDate date, String status) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.date = date;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public Long getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public LocalDate getDate() { return date; }
    public String getStatus() { return status; }
}
