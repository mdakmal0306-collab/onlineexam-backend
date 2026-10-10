package com.example.examination.repository;

import com.example.examination.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByStatusOrderByCreatedAtDesc(String status);
    List<Exam> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}
