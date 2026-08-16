package com.studentmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studentmanagement.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudent_Id(Long studentId);

    List<Attendance> findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);

    List<Attendance> findBySubject_Id(Long subjectId);

    boolean existsByStudent_IdAndSubject_IdAndDate(Long studentId, Long subjectId, java.time.LocalDate date);
}
