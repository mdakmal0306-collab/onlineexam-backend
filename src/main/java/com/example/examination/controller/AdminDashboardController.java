package com.example.examination.controller;

import com.example.examination.dto.ApiResponse;
import com.example.examination.dto.DashboardStatsDTO;
import com.example.examination.dto.ExamStatDTO;
import com.example.examination.dto.UserDTO;
import com.example.examination.service.DashboardService;
import com.example.examination.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    public AdminDashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Dashboard statistics retrieved", stats));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<ExamStatDTO>>> getAllExamStats() {
        List<ExamStatDTO> stats = dashboardService.getAllExamStatistics();
        return ResponseEntity.ok(ApiResponse.ok("Exam statistics retrieved", stats));
    }

    @GetMapping("/statistics/exams/{examId}")
    public ResponseEntity<ApiResponse<ExamStatDTO>> getExamStatById(@PathVariable Long examId) {
        ExamStatDTO stat = dashboardService.getExamStatisticById(examId);
        return ResponseEntity.ok(ApiResponse.ok("Exam statistics retrieved", stat));
    }

    @GetMapping("/statistics/students")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getStudentStats() {
        List<UserDTO> students = userService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.ok("Student statistics retrieved", students));
    }
}
