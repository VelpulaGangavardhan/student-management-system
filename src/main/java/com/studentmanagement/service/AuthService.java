package com.studentmanagement.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.dto.request.LoginRequestDTO;
import com.studentmanagement.dto.request.StudentRegistrationRequestDTO;
import com.studentmanagement.dto.response.LoginResponseDTO;
import com.studentmanagement.dto.response.RegistrationResponseDTO;
import com.studentmanagement.entity.Department;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, StudentRepository studentRepository,
                        DepartmentService departmentService, PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        // Delegates to Spring Security's AuthenticationManager, which uses
        // CustomUserDetailsService + the configured PasswordEncoder to check
        // the credentials. Throws BadCredentialsException on mismatch, which
        // GlobalExceptionHandler turns into a clean 401 response.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user vanished mid-request"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponseDTO(token, user.getUsername(), user.getRole().name());
    }

    /**
     * Public self-registration - STUDENT accounts only. The role is never
     * taken from the request (StudentRegistrationRequestDTO has no role
     * field at all), so there is no way for a caller - however the request
     * is crafted - to register as ADMIN or TEACHER through this endpoint.
     * TEACHER/ADMIN accounts are created exclusively by an admin via
     * POST /api/users or POST /api/teachers, both of which already require
     * ROLE_ADMIN.
     *
     * @Transactional so that a failure creating the Student record (e.g. a
     * bad department ID slipping past validation) rolls back the User
     * record too - no orphaned login-only accounts.
     */
    @Transactional
    public RegistrationResponseDTO registerStudent(StudentRegistrationRequestDTO request) {
        // Email is used as the username - the login form's "username" field
        // and this registration form's "email" field are the same credential.
        String username = request.getEmail();

        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new DuplicateResourceException("Student ID is already registered: " + request.getStudentId());
        }

        Department department = departmentService.getEntityById(request.getDepartmentId());

        User user = new User(username, request.getEmail(), passwordEncoder.encode(request.getPassword()), Role.STUDENT);
        user = userRepository.save(user);

        Student student = new Student();
        student.setStudentId(request.getStudentId());
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null && !request.getGender().isBlank()) {
            student.setGender(Student.Gender.valueOf(request.getGender().toUpperCase()));
        }
        student.setAddress(request.getAddress());
        student.setDepartment(department);
        student.setYear(request.getYear());
        student.setSemester(request.getSemester());
        student.setCgpa(request.getCgpa());
        student.setAdmissionDate(java.time.LocalDate.now());
        student.setStatus(Student.StudentStatus.ACTIVE);
        student.setUser(user);

        studentRepository.save(student);

        return new RegistrationResponseDTO(true, "Student account created successfully");
    }
}
