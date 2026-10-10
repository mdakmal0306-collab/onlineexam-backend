package com.example.examination.controller;

import com.example.examination.dto.ApiResponse;
import com.example.examination.dto.UserDTO;
import com.example.examination.dto.UserProfileUpdateDTO;
import com.example.examination.security.UserPrincipal;
import com.example.examination.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final UserService userService;

    public StudentController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllStudents() {
        List<UserDTO> students = userService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.ok("Students retrieved successfully", students));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getStudentById(@PathVariable Long id) {
        UserDTO student = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok("Student details retrieved", student));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        userService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.ok("Student deleted successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getMyProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserDTO student = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved successfully", student));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateMyProfile(@AuthenticationPrincipal UserPrincipal currentUser,
                                                                @Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        UserDTO updated = userService.updateUserProfile(currentUser.getId(), updateDTO);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateStudentByAdmin(@PathVariable Long id,
                                                                     @Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        UserDTO updated = userService.updateUserProfile(id, updateDTO);
        return ResponseEntity.ok(ApiResponse.ok("Student updated successfully", updated));
    }
}
