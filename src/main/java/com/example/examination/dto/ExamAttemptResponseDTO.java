package com.example.examination.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamAttemptResponseDTO {
    private Long attemptId;
    private Long examId;
    private String examTitle;
    private String examSubject;
    private String examDescription;
    private Long studentId;
    private String studentName;
    private String studentIdCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Long remainingSeconds;
    private String status;
    private List<QuestionStudentDTO> questions;
    private Map<Long, String> savedAnswers = new HashMap<>();

    public ExamAttemptResponseDTO() {
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getExamSubject() {
        return examSubject;
    }

    public void setExamSubject(String examSubject) {
        this.examSubject = examSubject;
    }

    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentIdCode() {
        return studentIdCode;
    }

    public void setStudentIdCode(String studentIdCode) {
        this.studentIdCode = studentIdCode;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<QuestionStudentDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionStudentDTO> questions) {
        this.questions = questions;
    }

    public Map<Long, String> getSavedAnswers() {
        return savedAnswers;
    }

    public void setSavedAnswers(Map<Long, String> savedAnswers) {
        this.savedAnswers = savedAnswers;
    }
}
