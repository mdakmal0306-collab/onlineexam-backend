package com.example.examination.service;

import com.example.examination.dto.ExamCreateUpdateDTO;
import com.example.examination.dto.ExamDTO;
import com.example.examination.entity.Exam;
import com.example.examination.exception.BadRequestException;
import com.example.examination.exception.ResourceNotFoundException;
import com.example.examination.repository.ExamRepository;
import com.example.examination.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public ExamService(ExamRepository examRepository, QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
    }

    public List<ExamDTO> getAllExams() {
        return examRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExamDTO> getAvailableExams() {
        return examRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()) || "UPCOMING".equalsIgnoreCase(e.getStatus()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ExamDTO getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));
        return mapToDTO(exam);
    }

    public Exam getExamEntityById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));
    }

    @Transactional
    public ExamDTO createExam(ExamCreateUpdateDTO dto) {
        validateExamData(dto);

        Exam exam = new Exam();
        exam.setTitle(dto.getTitle().trim());
        exam.setDescription(dto.getDescription());
        exam.setSubject(dto.getSubject().trim());
        exam.setExamDate(dto.getExamDate());
        exam.setStartTime(dto.getStartTime());
        exam.setDuration(dto.getDuration());
        exam.setTotalMarks(dto.getTotalMarks());
        exam.setPassingMarks(dto.getPassingMarks());
        exam.setStatus(dto.getStatus() != null ? dto.getStatus() : "UPCOMING");

        Exam savedExam = examRepository.save(exam);
        return mapToDTO(savedExam);
    }

    @Transactional
    public ExamDTO updateExam(Long id, ExamCreateUpdateDTO dto) {
        validateExamData(dto);

        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));

        exam.setTitle(dto.getTitle().trim());
        exam.setDescription(dto.getDescription());
        exam.setSubject(dto.getSubject().trim());
        exam.setExamDate(dto.getExamDate());
        exam.setStartTime(dto.getStartTime());
        exam.setDuration(dto.getDuration());
        exam.setTotalMarks(dto.getTotalMarks());
        exam.setPassingMarks(dto.getPassingMarks());
        if (dto.getStatus() != null) {
            exam.setStatus(dto.getStatus());
        }

        Exam updatedExam = examRepository.save(exam);
        return mapToDTO(updatedExam);
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));
        examRepository.delete(exam);
    }

    private void validateExamData(ExamCreateUpdateDTO dto) {
        if (dto.getDuration() <= 0) {
            throw new BadRequestException("Duration must be greater than 0 minutes");
        }
        if (dto.getTotalMarks() <= 0) {
            throw new BadRequestException("Total marks must be greater than 0");
        }
        if (dto.getPassingMarks() <= 0) {
            throw new BadRequestException("Passing marks must be greater than 0");
        }
        if (dto.getPassingMarks() > dto.getTotalMarks()) {
            throw new BadRequestException("Passing marks cannot exceed total examination marks");
        }
    }

    public ExamDTO mapToDTO(Exam exam) {
        ExamDTO dto = new ExamDTO();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setDescription(exam.getDescription());
        dto.setSubject(exam.getSubject());
        dto.setExamDate(exam.getExamDate());
        dto.setStartTime(exam.getStartTime());
        dto.setDuration(exam.getDuration());
        dto.setTotalMarks(exam.getTotalMarks());
        dto.setPassingMarks(exam.getPassingMarks());
        dto.setStatus(exam.getStatus());
        dto.setCreatedAt(exam.getCreatedAt());

        long qCount = questionRepository.countByExamId(exam.getId());
        dto.setQuestionCount((int) qCount);

        return dto;
    }
}
