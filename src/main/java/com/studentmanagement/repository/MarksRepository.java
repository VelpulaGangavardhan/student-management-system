package com.studentmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studentmanagement.entity.Marks;

public interface MarksRepository extends JpaRepository<Marks, Long> {

    List<Marks> findByStudent_Id(Long studentId);

    List<Marks> findBySubject_Id(Long subjectId);

    List<Marks> findByExam_Id(Long examId);

    List<Marks> findByStudent_IdAndExam_Id(Long studentId, Long examId);

    boolean existsByStudent_IdAndSubject_IdAndExam_Id(Long studentId, Long subjectId, Long examId);
}
