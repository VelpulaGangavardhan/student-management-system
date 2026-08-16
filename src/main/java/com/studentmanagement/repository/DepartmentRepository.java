package com.studentmanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.studentmanagement.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByCode(String code);

    Page<Department> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);
}
