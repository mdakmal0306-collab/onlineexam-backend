package com.example.examination.controller;

import com.example.examination.dto.*;
import com.example.examination.security.UserPrincipal;
import com.example.examination.service.ExamAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ExamAttemptController {

    private final ExamAttemptService examAttemptService;

    public ExamAttemptController(ExamAttemptService examAttemptService) {
        this.examAttemptService = examAttemptService;
    }

    @PostMapping("/api/exams/{examId}/start")
    public ResponseEntity<ApiResponse<ExamAttemptResponseDTO>> startExam(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ExamAttemptResponseDTO attempt = examAttemptService.startExam(examId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Exam started successfully", attempt));
    }

    @GetMapping("/api/attempts/{attemptId}")
    public ResponseEntity<ApiResponse<ExamAttemptResponseDTO>> getAttemptDetails(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        ExamAttemptResponseDTO attempt = examAttemptService.getAttemptDetails(attemptId, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Attempt details retrieved", attempt));
    }

    @GetMapping("/api/attempts/{attemptId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionStudentDTO>>> getAttemptQuestions(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        ExamAttemptResponseDTO attempt = examAttemptService.getAttemptDetails(attemptId, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Attempt questions retrieved", attempt.getQuestions()));
    }

    @PutMapping("/api/attempts/{attemptId}/answers/{questionId}")
    public ResponseEntity<ApiResponse<Void>> saveAnswer(
            @PathVariable Long attemptId,
            @PathVariable Long questionId,
            @RequestBody(required = false) AnswerSaveRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String selected = request != null ? request.getSelectedAnswer() : null;
        examAttemptService.saveAnswer(attemptId, questionId, selected, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Answer saved"));
    }

    @PostMapping("/api/attempts/{attemptId}/submit")
    public ResponseEntity<ApiResponse<ResultResponseDTO>> submitExam(
            @PathVariable Long attemptId,
            @RequestBody(required = false) ExamSubmissionRequestDTO submissionRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ResultResponseDTO result = examAttemptService.submitExam(attemptId, submissionRequest, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Examination submitted and evaluated successfully", result));
    }
}
