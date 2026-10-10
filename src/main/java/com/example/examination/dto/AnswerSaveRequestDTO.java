package com.example.examination.dto;

public class AnswerSaveRequestDTO {
    private String selectedAnswer; // A, B, C, D or null / empty to unselect

    public AnswerSaveRequestDTO() {
    }

    public AnswerSaveRequestDTO(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }
}
