package com.example.examination.service;

import com.example.examination.dto.ResultResponseDTO;
import com.example.examination.entity.Result;
import com.example.examination.exception.ResourceNotFoundException;
import com.example.examination.exception.UnauthorizedException;
import com.example.examination.repository.ResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final ExamAttemptService examAttemptService;

    public ResultService(ResultRepository resultRepository, ExamAttemptService examAttemptService) {
        this.resultRepository = resultRepository;
        this.examAttemptService = examAttemptService;
    }

    public List<ResultResponseDTO> getAllResults() {
        return resultRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(examAttemptService::buildResultResponse)
                .collect(Collectors.toList());
    }

    public List<ResultResponseDTO> getResultsByStudentId(Long studentId) {
        return resultRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(examAttemptService::buildResultResponse)
                .collect(Collectors.toList());
    }

    public List<ResultResponseDTO> getResultsByExamId(Long examId) {
        return resultRepository.findByExamIdOrderByCreatedAtDesc(examId)
                .stream()
                .map(examAttemptService::buildResultResponse)
                .collect(Collectors.toList());
    }

    public ResultResponseDTO getResultById(Long id, Long currentUserId, boolean isAdmin) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id: " + id));

        if (!isAdmin && !result.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not authorized to access this examination result");
        }

        return examAttemptService.buildResultResponse(result);
    }
}
