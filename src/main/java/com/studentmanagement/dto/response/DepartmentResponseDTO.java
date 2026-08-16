package com.studentmanagement.dto.response;

public class DepartmentResponseDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private long studentCount;
    private long teacherCount;
    private long subjectCount;

    public DepartmentResponseDTO(Long id, String name, String code, String description,
                                  long studentCount, long teacherCount, long subjectCount) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.studentCount = studentCount;
        this.teacherCount = teacherCount;
        this.subjectCount = subjectCount;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public long getStudentCount() {
        return studentCount;
    }

    public long getTeacherCount() {
        return teacherCount;
    }

    public long getSubjectCount() {
        return subjectCount;
    }
}
