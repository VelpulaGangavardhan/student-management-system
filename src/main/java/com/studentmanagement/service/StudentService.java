package com.studentmanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.request.StudentRequestDTO;
import com.studentmanagement.dto.response.StudentResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Student.Gender;
import com.studentmanagement.entity.Student.StudentStatus;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.StudentNotFoundException;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentService departmentService;
    private final UserRepository userRepository;

    public StudentService(StudentRepository studentRepository, DepartmentService departmentService,
                           UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.departmentService = departmentService;
        this.userRepository = userRepository;
    }

    public StudentResponseDTO createStudent(StudentRequestDTO request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A student with email " + request.getEmail() + " already exists");
        }
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new DuplicateResourceException("A student with ID " + request.getStudentId() + " already exists");
        }

        Student student = new Student();
        applyRequest(student, request);
        return toResponse(studentRepository.save(student));
    }

    public Student getEntityById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    public StudentResponseDTO getStudentById(Long id) {
        return toResponse(getEntityById(id));
    }

    public Student getEntityByUsername(String username) {
        return studentRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile is linked to this account"));
    }

    public Page<StudentResponseDTO> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable).map(this::toResponse);
    }

    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {
        Student existing = getEntityById(id);

        if (!existing.getEmail().equalsIgnoreCase(request.getEmail())
                && studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A student with email " + request.getEmail() + " already exists");
        }

        applyRequest(existing, request);
        return toResponse(studentRepository.save(existing));
    }

    public void deleteStudent(Long id) {
        Student existing = getEntityById(id);
        studentRepository.delete(existing);
    }

    public Page<StudentResponseDTO> searchStudents(String keyword, Pageable pageable) {
        return studentRepository.searchStudents(keyword, pageable).map(this::toResponse);
    }

    public Page<StudentResponseDTO> filterByDepartment(Long departmentId, Pageable pageable) {
        return studentRepository.findByDepartment_Id(departmentId, pageable).map(this::toResponse);
    }

    public Page<StudentResponseDTO> filterByYear(Integer year, Pageable pageable) {
        return studentRepository.findByYear(year, pageable).map(this::toResponse);
    }

    public Page<StudentResponseDTO> filterByStatus(StudentStatus status, Pageable pageable) {
        return studentRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    private void applyRequest(Student student, StudentRequestDTO request) {
        Department department = departmentService.getEntityById(request.getDepartmentId());

        student.setStudentId(request.getStudentId());
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) {
            student.setGender(Gender.valueOf(request.getGender().toUpperCase()));
        }
        student.setAddress(request.getAddress());
        student.setDepartment(department);
        student.setYear(request.getYear());
        student.setSemester(request.getSemester());
        student.setCgpa(request.getCgpa());
        student.setAdmissionDate(request.getAdmissionDate());
        if (request.getStatus() != null) {
            student.setStatus(StudentStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> ResourceNotFoundException.of("User", request.getUserId()));
            student.setUser(user);
        }
    }

    public StudentResponseDTO toResponse(Student student) {
        return new StudentResponseDTO(
                student.getId(),
                student.getStudentId(),
                student.getName(),
                student.getEmail(),
                student.getPhone(),
                student.getDateOfBirth(),
                student.getGender() != null ? student.getGender().name() : null,
                student.getAddress(),
                student.getDepartment() != null ? student.getDepartment().getId() : null,
                student.getDepartment() != null ? student.getDepartment().getName() : null,
                student.getYear(),
                student.getSemester(),
                student.getCgpa(),
                student.getAdmissionDate(),
                student.getStatus() != null ? student.getStatus().name() : null);
    }
}
