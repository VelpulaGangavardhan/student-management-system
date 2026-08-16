package com.studentmanagement.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Student.StudentStatus;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    Optional<Student> findByUser_Username(String username);

    Optional<Student> findByUser_Id(Long userId);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.studentId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);

    Page<Student> findByDepartment_Id(Long departmentId, Pageable pageable);

    Page<Student> findByYear(Integer year, Pageable pageable);

    Page<Student> findByStatus(StudentStatus status, Pageable pageable);

    long countByDepartment_Id(Long departmentId);

    @Query("SELECT AVG(s.cgpa) FROM Student s WHERE s.cgpa IS NOT NULL")
    Double averageCgpa();
}
