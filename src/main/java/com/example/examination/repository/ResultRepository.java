package com.example.examination.repository;

import com.example.examination.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Result> findByExamIdOrderByCreatedAtDesc(Long examId);
    Optional<Result> findByAttemptId(Long attemptId);
    Optional<Result> findByIdAndStudentId(Long id, Long studentId);
    List<Result> findAllByOrderByCreatedAtDesc();

    @Query("SELECT AVG(r.percentage) FROM Result r")
    Double calculateAveragePercentage();

    @Query("SELECT AVG(r.obtainedMarks) FROM Result r WHERE r.exam.id = :examId")
    Double calculateAverageMarksByExamId(Long examId);

    long countByResultStatus(String resultStatus);
    long countByExamIdAndResultStatus(Long examId, String resultStatus);
}
