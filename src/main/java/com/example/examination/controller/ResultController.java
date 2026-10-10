package com.example.examination.controller;

import com.example.examination.dto.ApiResponse;
import com.example.examination.dto.ResultResponseDTO;
import com.example.examination.security.UserPrincipal;
import com.example.examination.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultResponseDTO>>> getAllResults() {
        List<ResultResponseDTO> results = resultService.getAllResults();
        return ResponseEntity.ok(ApiResponse.ok("All examination results retrieved", results));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ResultResponseDTO>>> getMyResults(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ResultResponseDTO> results = resultService.getResultsByStudentId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Your examination results retrieved", results));
    }

    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<List<ResultResponseDTO>>> getMyHistory(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ResultResponseDTO> history = resultService.getResultsByStudentId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Your examination history retrieved", history));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<ResultResponseDTO>>> getStudentResults(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUser.getId().equals(studentId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("You cannot view results of another student"));
        }

        List<ResultResponseDTO> results = resultService.getResultsByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.ok("Student examination results retrieved", results));
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultResponseDTO>>> getExamResults(@PathVariable Long examId) {
        List<ResultResponseDTO> results = resultService.getResultsByExamId(examId);
        return ResponseEntity.ok(ApiResponse.ok("Examination results retrieved", results));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResultResponseDTO>> getResultById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        ResultResponseDTO result = resultService.getResultById(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Result details retrieved", result));
    }
}
