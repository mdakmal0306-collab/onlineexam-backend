package com.example.examination.dto;

import java.util.ArrayList;
import java.util.List;

public class ExamSubmissionRequestDTO {
    private List<AnswerItemDTO> answers = new ArrayList<>();

    public ExamSubmissionRequestDTO() {
    }

    public ExamSubmissionRequestDTO(List<AnswerItemDTO> answers) {
        this.answers = answers;
    }

    public List<AnswerItemDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerItemDTO> answers) {
        this.answers = answers;
    }
}
