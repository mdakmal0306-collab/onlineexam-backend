package com.example.examination.controller;

import com.example.examination.dto.ApiResponse;
import com.example.examination.dto.ExamCreateUpdateDTO;
import com.example.examination.dto.ExamDTO;
import com.example.examination.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamDTO>>> getAllExams() {
        List<ExamDTO> exams = examService.getAllExams();
        return ResponseEntity.ok(ApiResponse.ok("Examinations retrieved successfully", exams));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ExamDTO>>> getAvailableExams() {
        List<ExamDTO> exams = examService.getAvailableExams();
        return ResponseEntity.ok(ApiResponse.ok("Available examinations retrieved", exams));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamDTO>> getExamById(@PathVariable Long id) {
        ExamDTO exam = examService.getExamById(id);
        return ResponseEntity.ok(ApiResponse.ok("Examination details retrieved", exam));
    }

    @GetMapping("/{id}/instructions")
    public ResponseEntity<ApiResponse<ExamDTO>> getExamInstructions(@PathVariable Long id) {
        ExamDTO exam = examService.getExamById(id);
        return ResponseEntity.ok(ApiResponse.ok("Examination instructions retrieved", exam));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ExamDTO>> createExam(@Valid @RequestBody ExamCreateUpdateDTO examDTO) {
        ExamDTO created = examService.createExam(examDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Examination created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ExamDTO>> updateExam(@PathVariable Long id,
                                                           @Valid @RequestBody ExamCreateUpdateDTO examDTO) {
        ExamDTO updated = examService.updateExam(id, examDTO);
        return ResponseEntity.ok(ApiResponse.ok("Examination updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.ok(ApiResponse.ok("Examination deleted successfully"));
    }
}
