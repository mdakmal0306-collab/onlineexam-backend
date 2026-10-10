package com.example.examination.controller;

import com.example.examination.dto.ApiResponse;
import com.example.examination.dto.QuestionAdminDTO;
import com.example.examination.dto.QuestionCreateUpdateDTO;
import com.example.examination.security.UserPrincipal;
import com.example.examination.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/api/exams/{examId}/questions")
    public ResponseEntity<ApiResponse<?>> getQuestionsByExam(@PathVariable Long examId,
                                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser != null && currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            List<QuestionAdminDTO> questions = questionService.getQuestionsByExamIdForAdmin(examId);
            return ResponseEntity.ok(ApiResponse.ok("Exam questions retrieved", questions));
        } else {
            // Student question list - NEVER includes correct answer
            return ResponseEntity.ok(ApiResponse.ok("Exam questions retrieved",
                    questionService.getQuestionsByExamIdForStudent(examId)));
        }
    }

    @PostMapping("/api/exams/{examId}/questions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<QuestionAdminDTO>> addQuestion(@PathVariable Long examId,
                                                                     @Valid @RequestBody QuestionCreateUpdateDTO dto) {
        QuestionAdminDTO created = questionService.addQuestion(examId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Question added successfully", created));
    }

    @GetMapping("/api/questions/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<QuestionAdminDTO>> getQuestionById(@PathVariable Long id) {
        QuestionAdminDTO question = questionService.getQuestionById(id);
        return ResponseEntity.ok(ApiResponse.ok("Question details retrieved", question));
    }

    @PutMapping("/api/questions/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<QuestionAdminDTO>> updateQuestion(@PathVariable Long id,
                                                                        @Valid @RequestBody QuestionCreateUpdateDTO dto) {
        QuestionAdminDTO updated = questionService.updateQuestion(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Question updated successfully", updated));
    }

    @DeleteMapping("/api/questions/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.ok("Question deleted successfully"));
    }
}
