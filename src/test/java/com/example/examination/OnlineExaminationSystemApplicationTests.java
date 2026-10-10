package com.example.examination;

import com.example.examination.dto.AuthRequest;
import com.example.examination.dto.AuthResponse;
import com.example.examination.dto.ExamDTO;
import com.example.examination.dto.RegisterRequest;
import com.example.examination.service.AuthService;
import com.example.examination.service.ExamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class OnlineExaminationSystemApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private ExamService examService;

    @Test
    void contextLoads() {
        assertNotNull(authService);
        assertNotNull(examService);
    }

    @Test
    void testAdminLogin() {
        AuthRequest req = new AuthRequest("admin@example.com", "admin123");
        AuthResponse res = authService.login(req);
        assertNotNull(res);
        assertNotNull(res.getToken());
        assertEquals("ROLE_ADMIN", res.getRole());
    }

    @Test
    void testStudentLogin() {
        AuthRequest req = new AuthRequest("student@example.com", "student123");
        AuthResponse res = authService.login(req);
        assertNotNull(res);
        assertNotNull(res.getToken());
        assertEquals("ROLE_STUDENT", res.getRole());
    }

    @Test
    void testStudentRegistration() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test Registration Student");
        req.setStudentId("STU999");
        req.setEmail("teststudent999@example.com");
        req.setPhone("9123456780");
        req.setPassword("pass123");
        req.setConfirmPassword("pass123");

        AuthResponse res = authService.registerStudent(req);
        assertNotNull(res);
        assertNotNull(res.getToken());
        assertEquals("ROLE_STUDENT", res.getRole());
        assertEquals("STU999", res.getStudentId());
    }

    @Test
    void testAvailableExams() {
        var exams = examService.getAvailableExams();
        assertNotNull(exams);
        assertFalse(exams.isEmpty());
    }
}
