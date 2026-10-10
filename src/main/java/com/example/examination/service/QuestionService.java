package com.example.examination.service;

import com.example.examination.dto.QuestionAdminDTO;
import com.example.examination.dto.QuestionCreateUpdateDTO;
import com.example.examination.dto.QuestionStudentDTO;
import com.example.examination.entity.Exam;
import com.example.examination.entity.Question;
import com.example.examination.exception.BadRequestException;
import com.example.examination.exception.ResourceNotFoundException;
import com.example.examination.repository.ExamRepository;
import com.example.examination.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    public QuestionService(QuestionRepository questionRepository, ExamRepository examRepository) {
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
    }

    public List<QuestionAdminDTO> getQuestionsByExamIdForAdmin(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Examination not found with id: " + examId);
        }
        return questionRepository.findByExamIdOrderByIdAsc(examId)
                .stream()
                .map(this::mapToAdminDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionStudentDTO> getQuestionsByExamIdForStudent(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Examination not found with id: " + examId);
        }
        return questionRepository.findByExamIdOrderByIdAsc(examId)
                .stream()
                .map(this::mapToStudentDTO)
                .collect(Collectors.toList());
    }

    public QuestionAdminDTO getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        return mapToAdminDTO(question);
    }

    @Transactional
    public QuestionAdminDTO addQuestion(Long examId, QuestionCreateUpdateDTO dto) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + examId));

        validateQuestionData(dto);

        Question question = new Question();
        question.setExam(exam);
        question.setQuestionText(dto.getQuestionText().trim());
        question.setOptionA(dto.getOptionA().trim());
        question.setOptionB(dto.getOptionB().trim());
        question.setOptionC(dto.getOptionC().trim());
        question.setOptionD(dto.getOptionD().trim());
        question.setCorrectAnswer(dto.getCorrectAnswer().trim().toUpperCase());
        question.setMarks(dto.getMarks());

        Question savedQuestion = questionRepository.save(question);
        return mapToAdminDTO(savedQuestion);
    }

    @Transactional
    public QuestionAdminDTO updateQuestion(Long id, QuestionCreateUpdateDTO dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));

        validateQuestionData(dto);

        question.setQuestionText(dto.getQuestionText().trim());
        question.setOptionA(dto.getOptionA().trim());
        question.setOptionB(dto.getOptionB().trim());
        question.setOptionC(dto.getOptionC().trim());
        question.setOptionD(dto.getOptionD().trim());
        question.setCorrectAnswer(dto.getCorrectAnswer().trim().toUpperCase());
        question.setMarks(dto.getMarks());

        Question updatedQuestion = questionRepository.save(question);
        return mapToAdminDTO(updatedQuestion);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        questionRepository.delete(question);
    }

    private void validateQuestionData(QuestionCreateUpdateDTO dto) {
        if (dto.getMarks() <= 0) {
            throw new BadRequestException("Marks must be at least 1");
        }
        String ans = dto.getCorrectAnswer().trim().toUpperCase();
        if (!ans.equals("A") && !ans.equals("B") && !ans.equals("C") && !ans.equals("D")) {
            throw new BadRequestException("Correct answer must be one of: A, B, C, or D");
        }
    }

    public QuestionAdminDTO mapToAdminDTO(Question q) {
        return new QuestionAdminDTO(
                q.getId(),
                q.getExam().getId(),
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getCorrectAnswer(),
                q.getMarks()
        );
    }

    public QuestionStudentDTO mapToStudentDTO(Question q) {
        return new QuestionStudentDTO(
                q.getId(),
                q.getExam().getId(),
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getMarks()
        );
    }
}
