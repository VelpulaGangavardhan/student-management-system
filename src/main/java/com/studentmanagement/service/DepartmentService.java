package com.studentmanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.request.DepartmentRequestDTO;
import com.studentmanagement.dto.response.DepartmentResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.SubjectRepository;
import com.studentmanagement.repository.TeacherRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;

    public DepartmentService(DepartmentRepository departmentRepository, StudentRepository studentRepository,
                              TeacherRepository teacherRepository, SubjectRepository subjectRepository) {
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
    }

    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO request) {
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("A department with code " + request.getCode() + " already exists");
        }
        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        return toResponse(departmentRepository.save(department));
    }

    public DepartmentResponseDTO getDepartmentById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public Department getEntityById(Long id) {
        return findOrThrow(id);
    }

    public Page<DepartmentResponseDTO> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<DepartmentResponseDTO> searchDepartments(String keyword, Pageable pageable) {
        return departmentRepository
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::toResponse);
    }

    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO request) {
        Department department = findOrThrow(id);

        if (!department.getCode().equalsIgnoreCase(request.getCode())
                && departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("A department with code " + request.getCode() + " already exists");
        }

        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        return toResponse(departmentRepository.save(department));
    }

    public void deleteDepartment(Long id) {
        Department department = findOrThrow(id);
        departmentRepository.delete(department);
    }

    private Department findOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", id));
    }

    private DepartmentResponseDTO toResponse(Department department) {
        long students = studentRepository.countByDepartment_Id(department.getId());
        long teachers = teacherRepository
                .findByDepartment_Id(department.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();
        long subjects = subjectRepository
                .findByDepartment_Id(department.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();
        return new DepartmentResponseDTO(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                students,
                teachers,
                subjects);
    }
}
