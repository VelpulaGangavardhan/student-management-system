package com.studentmanagement.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studentmanagement.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByCode(String code);

    @Query("SELECT s FROM Subject s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Subject> searchSubjects(@Param("keyword") String keyword, Pageable pageable);

    Page<Subject> findByDepartment_Id(Long departmentId, Pageable pageable);

    Page<Subject> findBySemester(Integer semester, Pageable pageable);

    List<Subject> findByTeacher_Id(Long teacherId);
}
