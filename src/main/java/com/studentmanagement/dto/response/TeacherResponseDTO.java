package com.studentmanagement.dto.response;

public class TeacherResponseDTO {

    private Long id;
    private String teacherId;
    private String name;
    private String email;
    private String phone;
    private String qualification;
    private String specialization;
    private Long departmentId;
    private String departmentName;

    public TeacherResponseDTO(Long id, String teacherId, String name, String email, String phone,
                               String qualification, String specialization, Long departmentId, String departmentName) {
        this.id = id;
        this.teacherId = teacherId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.qualification = qualification;
        this.specialization = specialization;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public Long getId() { return id; }
    public String getTeacherId() { return teacherId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getQualification() { return qualification; }
    public String getSpecialization() { return specialization; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
}
