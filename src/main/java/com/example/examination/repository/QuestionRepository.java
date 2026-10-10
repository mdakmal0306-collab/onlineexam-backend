package com.example.examination.repository;

import com.example.examination.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamIdOrderByIdAsc(Long examId);
    long countByExamId(Long examId);
    void deleteByExamId(Long examId);
}
