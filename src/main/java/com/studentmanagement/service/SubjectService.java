package com.studentmanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.request.SubjectRequestDTO;
import com.studentmanagement.dto.response.SubjectResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Subject;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.SubjectRepository;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentService departmentService;
    private final TeacherService teacherService;

    public SubjectService(SubjectRepository subjectRepository, DepartmentService departmentService,
                           TeacherService teacherService) {
        this.subjectRepository = subjectRepository;
        this.departmentService = departmentService;
        this.teacherService = teacherService;
    }

    public SubjectResponseDTO createSubject(SubjectRequestDTO request) {
        if (subjectRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("A subject with code " + request.getCode() + " already exists");
        }
        Subject subject = new Subject();
        applyRequest(subject, request);
        return toResponse(subjectRepository.save(subject));
    }

    public Subject getEntityById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", id));
    }

    public SubjectResponseDTO getSubjectById(Long id) {
        return toResponse(getEntityById(id));
    }

    public Page<SubjectResponseDTO> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<SubjectResponseDTO> searchSubjects(String keyword, Pageable pageable) {
        return subjectRepository.searchSubjects(keyword, pageable).map(this::toResponse);
    }

    public Page<SubjectResponseDTO> filterByDepartment(Long departmentId, Pageable pageable) {
        return subjectRepository.findByDepartment_Id(departmentId, pageable).map(this::toResponse);
    }

    public Page<SubjectResponseDTO> filterBySemester(Integer semester, Pageable pageable) {
        return subjectRepository.findBySemester(semester, pageable).map(this::toResponse);
    }

    public java.util.List<Subject> getSubjectsByTeacher(Long teacherId) {
        return subjectRepository.findByTeacher_Id(teacherId);
    }

    public SubjectResponseDTO updateSubject(Long id, SubjectRequestDTO request) {
        Subject existing = getEntityById(id);
        if (!existing.getCode().equalsIgnoreCase(request.getCode())
                && subjectRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("A subject with code " + request.getCode() + " already exists");
        }
        applyRequest(existing, request);
        return toResponse(subjectRepository.save(existing));
    }

    public void deleteSubject(Long id) {
        subjectRepository.delete(getEntityById(id));
    }

    private void applyRequest(Subject subject, SubjectRequestDTO request) {
        Department department = departmentService.getEntityById(request.getDepartmentId());
        subject.setCode(request.getCode());
        subject.setName(request.getName());
        subject.setCredits(request.getCredits());
        subject.setSemester(request.getSemester());
        subject.setDepartment(department);
        if (request.getTeacherId() != null) {
            Teacher teacher = teacherService.getEntityById(request.getTeacherId());
            subject.setTeacher(teacher);
        } else {
            subject.setTeacher(null);
        }
    }

    public SubjectResponseDTO toResponse(Subject subject) {
        return new SubjectResponseDTO(
                subject.getId(),
                subject.getCode(),
                subject.getName(),
                subject.getCredits(),
                subject.getSemester(),
                subject.getDepartment() != null ? subject.getDepartment().getId() : null,
                subject.getDepartment() != null ? subject.getDepartment().getName() : null,
                subject.getTeacher() != null ? subject.getTeacher().getId() : null,
                subject.getTeacher() != null ? subject.getTeacher().getName() : null);
    }
}
