package com.studentmanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.dto.request.TeacherRequestDTO;
import com.studentmanagement.dto.response.TeacherResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.TeacherRepository;
import com.studentmanagement.repository.UserRepository;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentService departmentService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository, DepartmentService departmentService,
                           UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.departmentService = departmentService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @Transactional so that if creating the Teacher record fails after a
     * new User login account was created for it (see applyRequest below),
     * the User insert is rolled back too - no orphaned login-only accounts.
     */
    @Transactional
    public TeacherResponseDTO createTeacher(TeacherRequestDTO request) {
        if (teacherRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A teacher with email " + request.getEmail() + " already exists");
        }
        if (teacherRepository.existsByTeacherId(request.getTeacherId())) {
            throw new DuplicateResourceException("A teacher with ID " + request.getTeacherId() + " already exists");
        }
        Teacher teacher = new Teacher();
        applyRequest(teacher, request);
        return toResponse(teacherRepository.save(teacher));
    }

    public Teacher getEntityById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", id));
    }

    public Teacher getEntityByUsername(String username) {
        return teacherRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("No teacher profile is linked to this account"));
    }

    public TeacherResponseDTO getTeacherById(Long id) {
        return toResponse(getEntityById(id));
    }

    public Page<TeacherResponseDTO> getAllTeachers(Pageable pageable) {
        return teacherRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<TeacherResponseDTO> searchTeachers(String keyword, Pageable pageable) {
        return teacherRepository.searchTeachers(keyword, pageable).map(this::toResponse);
    }

    public Page<TeacherResponseDTO> filterByDepartment(Long departmentId, Pageable pageable) {
        return teacherRepository.findByDepartment_Id(departmentId, pageable).map(this::toResponse);
    }

    @Transactional
    public TeacherResponseDTO updateTeacher(Long id, TeacherRequestDTO request) {
        Teacher existing = getEntityById(id);
        if (!existing.getEmail().equalsIgnoreCase(request.getEmail())
                && teacherRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A teacher with email " + request.getEmail() + " already exists");
        }
        applyRequest(existing, request);
        return toResponse(teacherRepository.save(existing));
    }

    public void deleteTeacher(Long id) {
        teacherRepository.delete(getEntityById(id));
    }

    private void applyRequest(Teacher teacher, TeacherRequestDTO request) {
        Department department = departmentService.getEntityById(request.getDepartmentId());
        teacher.setTeacherId(request.getTeacherId());
        teacher.setName(request.getName());
        teacher.setEmail(request.getEmail());
        teacher.setPhone(request.getPhone());
        teacher.setQualification(request.getQualification());
        teacher.setSpecialization(request.getSpecialization());
        teacher.setDepartment(department);

        if (request.getUserId() != null) {
            // Link to an already-existing login account.
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> ResourceNotFoundException.of("User", request.getUserId()));
            teacher.setUser(user);
        } else if (request.getPassword() != null && !request.getPassword().isBlank() && teacher.getUser() == null) {
            // Admin is provisioning a brand-new login account for this
            // teacher in the same request - role is hardcoded to TEACHER,
            // never taken from client input.
            if (userRepository.existsByUsername(request.getEmail()) || userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("A user account with email " + request.getEmail() + " already exists");
            }
            User user = new User(request.getEmail(), request.getEmail(),
                    passwordEncoder.encode(request.getPassword()), Role.TEACHER);
            teacher.setUser(userRepository.save(user));
        }
    }

    public TeacherResponseDTO toResponse(Teacher teacher) {
        return new TeacherResponseDTO(
                teacher.getId(),
                teacher.getTeacherId(),
                teacher.getName(),
                teacher.getEmail(),
                teacher.getPhone(),
                teacher.getQualification(),
                teacher.getSpecialization(),
                teacher.getDepartment() != null ? teacher.getDepartment().getId() : null,
                teacher.getDepartment() != null ? teacher.getDepartment().getName() : null);
    }
}
