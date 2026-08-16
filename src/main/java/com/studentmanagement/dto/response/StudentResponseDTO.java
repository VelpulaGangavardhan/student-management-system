package com.studentmanagement.dto.response;

import java.time.LocalDate;

public class StudentResponseDTO {

    private Long id;
    private String studentId;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private Long departmentId;
    private String departmentName;
    private Integer year;
    private Integer semester;
    private Double cgpa;
    private LocalDate admissionDate;
    private String status;

    public StudentResponseDTO(Long id, String studentId, String name, String email, String phone,
                               LocalDate dateOfBirth, String gender, String address,
                               Long departmentId, String departmentName, Integer year, Integer semester,
                               Double cgpa, LocalDate admissionDate, String status) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.year = year;
        this.semester = semester;
        this.cgpa = cgpa;
        this.admissionDate = admissionDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public Integer getYear() { return year; }
    public Integer getSemester() { return semester; }
    public Double getCgpa() { return cgpa; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public String getStatus() { return status; }
}
