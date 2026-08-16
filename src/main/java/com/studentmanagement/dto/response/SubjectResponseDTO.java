package com.studentmanagement.dto.response;

public class SubjectResponseDTO {

    private Long id;
    private String code;
    private String name;
    private Integer credits;
    private Integer semester;
    private Long departmentId;
    private String departmentName;
    private Long teacherId;
    private String teacherName;

    public SubjectResponseDTO(Long id, String code, String name, Integer credits, Integer semester,
                               Long departmentId, String departmentName, Long teacherId, String teacherName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.semester = semester;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Integer getCredits() { return credits; }
    public Integer getSemester() { return semester; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public Long getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
}
