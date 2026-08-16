package com.studentmanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.studentmanagement.entity.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    Page<Exam> findBySemester(Integer semester, Pageable pageable);
}
