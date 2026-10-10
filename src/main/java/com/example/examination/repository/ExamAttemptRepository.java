package com.example.examination.repository;

import com.example.examination.entity.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByStudentIdOrderByStartTimeDesc(Long studentId);
    List<ExamAttempt> findByExamIdOrderByStartTimeDesc(Long examId);
    Optional<ExamAttempt> findByIdAndStudentId(Long id, Long studentId);
    Optional<ExamAttempt> findFirstByStudentIdAndExamIdAndStatus(Long studentId, Long examId, String status);
    boolean existsByStudentIdAndExamIdAndStatus(Long studentId, Long examId, String status);
    long countByStatus(String status);
}
