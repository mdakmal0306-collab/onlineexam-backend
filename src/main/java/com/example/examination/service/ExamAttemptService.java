package com.example.examination.service;

import com.example.examination.dto.*;
import com.example.examination.entity.*;
import com.example.examination.exception.BadRequestException;
import com.example.examination.exception.ResourceNotFoundException;
import com.example.examination.exception.UnauthorizedException;
import com.example.examination.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamAttemptService {

    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ResultRepository resultRepository;
    private final UserRepository userRepository;
    private final QuestionService questionService;

    public ExamAttemptService(ExamAttemptRepository examAttemptRepository,
                              ExamRepository examRepository,
                              QuestionRepository questionRepository,
                              AnswerRepository answerRepository,
                              ResultRepository resultRepository,
                              UserRepository userRepository,
                              QuestionService questionService) {
        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.questionService = questionService;
    }

    @Transactional
    public ExamAttemptResponseDTO startExam(Long examId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + examId));

        if ("COMPLETED".equalsIgnoreCase(exam.getStatus())) {
            throw new BadRequestException("This examination has been marked as COMPLETED and is no longer accepting attempts.");
        }

        // Check if there is an existing in-progress attempt
        Optional<ExamAttempt> existingAttemptOpt = examAttemptRepository
                .findFirstByStudentIdAndExamIdAndStatus(studentId, examId, "IN_PROGRESS");

        ExamAttempt attempt;
        if (existingAttemptOpt.isPresent()) {
            attempt = existingAttemptOpt.get();
            // Check if deadline already passed
            if (LocalDateTime.now().isAfter(attempt.getEndTime())) {
                // Auto-finalize the expired attempt
                evaluateAndFinalizeAttempt(attempt, new ArrayList<>());
                // Create a fresh attempt or throw based on single/multi attempt policy
                attempt = createNewAttempt(student, exam);
            }
        } else {
            attempt = createNewAttempt(student, exam);
        }

        return buildAttemptResponse(attempt);
    }

    private ExamAttempt createNewAttempt(User student, Exam exam) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusMinutes(exam.getDuration());

        ExamAttempt newAttempt = new ExamAttempt();
        newAttempt.setStudent(student);
        newAttempt.setExam(exam);
        newAttempt.setStartTime(now);
        newAttempt.setEndTime(deadline);
        newAttempt.setStatus("IN_PROGRESS");

        return examAttemptRepository.save(newAttempt);
    }

    @Transactional
    public ExamAttemptResponseDTO getAttemptDetails(Long attemptId, Long currentUserId, boolean isAdmin) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found with id: " + attemptId));

        if (!isAdmin && !attempt.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not authorized to view this exam attempt");
        }

        // Check for server-side timeout auto-finalization
        if ("IN_PROGRESS".equals(attempt.getStatus()) && LocalDateTime.now().isAfter(attempt.getEndTime())) {
            evaluateAndFinalizeAttempt(attempt, new ArrayList<>());
        }

        return buildAttemptResponse(attempt);
    }

    @Transactional
    public void saveAnswer(Long attemptId, Long questionId, String selectedAnswer, Long currentUserId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found with id: " + attemptId));

        if (!attempt.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not authorized to modify this examination attempt");
        }

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new BadRequestException("This examination attempt is already " + attempt.getStatus());
        }

        // Server-side deadline check
        if (LocalDateTime.now().isAfter(attempt.getEndTime())) {
            evaluateAndFinalizeAttempt(attempt, new ArrayList<>());
            throw new BadRequestException("Examination deadline has expired. Your attempt has been automatically submitted.");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        if (!question.getExam().getId().equals(attempt.getExam().getId())) {
            throw new BadRequestException("Question does not belong to this examination");
        }

        Optional<Answer> existingAnswerOpt = answerRepository.findByAttemptIdAndQuestionId(attemptId, questionId);
        Answer answer;
        if (existingAnswerOpt.isPresent()) {
            answer = existingAnswerOpt.get();
        } else {
            answer = new Answer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
        }

        String formattedChoice = (selectedAnswer != null && !selectedAnswer.trim().isEmpty()) 
                ? selectedAnswer.trim().toUpperCase() : null;
        answer.setSelectedAnswer(formattedChoice);
        answerRepository.save(answer);
    }

    @Transactional
    public ResultResponseDTO submitExam(Long attemptId, ExamSubmissionRequestDTO submissionRequest, Long currentUserId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found with id: " + attemptId));

        if (!attempt.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not authorized to submit this examination attempt");
        }

        // If already submitted, return the existing result idempotently
        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            Optional<Result> existingResult = resultRepository.findByAttemptId(attemptId);
            if (existingResult.isPresent()) {
                return buildResultResponse(existingResult.get());
            }
        }

        List<AnswerItemDTO> answers = (submissionRequest != null && submissionRequest.getAnswers() != null)
                ? submissionRequest.getAnswers() : new ArrayList<>();

        Result result = evaluateAndFinalizeAttempt(attempt, answers);
        return buildResultResponse(result);
    }

    @Transactional
    public Result evaluateAndFinalizeAttempt(ExamAttempt attempt, List<AnswerItemDTO> submittedAnswers) {
        // If already finalized, return existing result
        Optional<Result> existingResult = resultRepository.findByAttemptId(attempt.getId());
        if (existingResult.isPresent()) {
            return existingResult.get();
        }

        Exam exam = attempt.getExam();
        List<Question> questions = questionRepository.findByExamIdOrderByIdAsc(exam.getId());

        // Process any answers passed in submission payload
        Map<Long, String> submissionAnswerMap = new HashMap<>();
        for (AnswerItemDTO item : submittedAnswers) {
            if (item.getQuestionId() != null && item.getSelectedAnswer() != null) {
                submissionAnswerMap.put(item.getQuestionId(), item.getSelectedAnswer().trim().toUpperCase());
            }
        }

        // Fetch existing saved answers
        List<Answer> existingAnswers = answerRepository.findByAttemptId(attempt.getId());
        Map<Long, Answer> answerMap = new HashMap<>();
        for (Answer a : existingAnswers) {
            answerMap.put(a.getQuestion().getId(), a);
        }

        int totalQuestions = questions.size();
        int attemptedCount = 0;
        int correctCount = 0;
        int incorrectCount = 0;
        int obtainedMarks = 0;
        int totalCalculatedMarks = 0;

        for (Question q : questions) {
            totalCalculatedMarks += q.getMarks();

            Answer answer = answerMap.get(q.getId());
            if (answer == null) {
                answer = new Answer();
                answer.setAttempt(attempt);
                answer.setQuestion(q);
            }

            // Check if submitted answers payload had an update
            if (submissionAnswerMap.containsKey(q.getId())) {
                answer.setSelectedAnswer(submissionAnswerMap.get(q.getId()));
            }

            String chosen = answer.getSelectedAnswer();
            if (chosen != null && !chosen.trim().isEmpty()) {
                attemptedCount++;
                if (chosen.equalsIgnoreCase(q.getCorrectAnswer())) {
                    answer.setIsCorrect(true);
                    correctCount++;
                    obtainedMarks += q.getMarks();
                } else {
                    answer.setIsCorrect(false);
                    incorrectCount++;
                }
            } else {
                answer.setSelectedAnswer(null);
                answer.setIsCorrect(false);
            }

            answerRepository.save(answer);
        }

        int totalMarks = (exam.getTotalMarks() != null && exam.getTotalMarks() > 0) ? exam.getTotalMarks() : totalCalculatedMarks;
        if (totalMarks == 0) totalMarks = totalCalculatedMarks > 0 ? totalCalculatedMarks : 1;

        int unansweredCount = totalQuestions - attemptedCount;
        double percentage = Math.round(((double) obtainedMarks / totalMarks) * 10000.0) / 100.0;
        String resultStatus = (obtainedMarks >= exam.getPassingMarks()) ? "PASS" : "FAIL";

        // Update attempt status
        attempt.setStatus("SUBMITTED");
        examAttemptRepository.save(attempt);

        // Create Result entity
        Result result = new Result();
        result.setStudent(attempt.getStudent());
        result.setExam(exam);
        result.setAttempt(attempt);
        result.setTotalQuestions(totalQuestions);
        result.setAttemptedQuestions(attemptedCount);
        result.setCorrectAnswers(correctCount);
        result.setIncorrectAnswers(incorrectCount);
        result.setUnansweredQuestions(unansweredCount);
        result.setTotalMarks(totalMarks);
        result.setObtainedMarks(obtainedMarks);
        result.setPercentage(percentage);
        result.setResultStatus(resultStatus);

        return resultRepository.save(result);
    }

    private ExamAttemptResponseDTO buildAttemptResponse(ExamAttempt attempt) {
        ExamAttemptResponseDTO dto = new ExamAttemptResponseDTO();
        dto.setAttemptId(attempt.getId());
        dto.setExamId(attempt.getExam().getId());
        dto.setExamTitle(attempt.getExam().getTitle());
        dto.setExamSubject(attempt.getExam().getSubject());
        dto.setExamDescription(attempt.getExam().getDescription());
        dto.setStudentId(attempt.getStudent().getId());
        dto.setStudentName(attempt.getStudent().getName());
        dto.setStudentIdCode(attempt.getStudent().getStudentId());
        dto.setStartTime(attempt.getStartTime());
        dto.setEndTime(attempt.getEndTime());
        dto.setDurationMinutes(attempt.getExam().getDuration());
        dto.setStatus(attempt.getStatus());

        LocalDateTime now = LocalDateTime.now();
        long remaining = Math.max(0, Duration.between(now, attempt.getEndTime()).getSeconds());
        dto.setRemainingSeconds(remaining);

        // Populate student question DTOs (NEVER with correct answers!)
        List<QuestionStudentDTO> questions = questionService.getQuestionsByExamIdForStudent(attempt.getExam().getId());
        dto.setQuestions(questions);

        // Populate saved answers map
        List<Answer> answers = answerRepository.findByAttemptId(attempt.getId());
        Map<Long, String> savedMap = new HashMap<>();
        for (Answer a : answers) {
            if (a.getSelectedAnswer() != null) {
                savedMap.put(a.getQuestion().getId(), a.getSelectedAnswer());
            }
        }
        dto.setSavedAnswers(savedMap);

        return dto;
    }

    public ResultResponseDTO buildResultResponse(Result result) {
        ResultResponseDTO dto = new ResultResponseDTO();
        dto.setId(result.getId());
        dto.setAttemptId(result.getAttempt() != null ? result.getAttempt().getId() : null);
        dto.setStudentId(result.getStudent().getId());
        dto.setStudentName(result.getStudent().getName());
        dto.setStudentIdCode(result.getStudent().getStudentId());
        dto.setExamId(result.getExam().getId());
        dto.setExamName(result.getExam().getTitle());
        dto.setExamSubject(result.getExam().getSubject());
        dto.setTotalQuestions(result.getTotalQuestions());
        dto.setAttemptedQuestions(result.getAttemptedQuestions());
        dto.setCorrectAnswers(result.getCorrectAnswers());
        dto.setIncorrectAnswers(result.getIncorrectAnswers());
        dto.setUnansweredQuestions(result.getUnansweredQuestions());
        dto.setTotalMarks(result.getTotalMarks());
        dto.setObtainedMarks(result.getObtainedMarks());
        dto.setPassingMarks(result.getExam().getPassingMarks());
        dto.setPercentage(result.getPercentage());
        dto.setResultStatus(result.getResultStatus());
        dto.setCreatedAt(result.getCreatedAt());

        // Build answer reviews
        if (result.getAttempt() != null) {
            List<Question> questions = questionRepository.findByExamIdOrderByIdAsc(result.getExam().getId());
            List<Answer> answers = answerRepository.findByAttemptId(result.getAttempt().getId());
            Map<Long, Answer> answerMap = answers.stream()
                    .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (a1, a2) -> a1));

            List<AnswerReviewDTO> reviews = new ArrayList<>();
            for (Question q : questions) {
                Answer a = answerMap.get(q.getId());
                String selected = (a != null) ? a.getSelectedAnswer() : null;
                boolean isCorrect = (a != null && Boolean.TRUE.equals(a.getIsCorrect()));

                reviews.add(new AnswerReviewDTO(
                        q.getId(),
                        q.getQuestionText(),
                        q.getOptionA(),
                        q.getOptionB(),
                        q.getOptionC(),
                        q.getOptionD(),
                        selected,
                        q.getCorrectAnswer(),
                        isCorrect,
                        q.getMarks()
                ));
            }
            dto.setAnswerReviews(reviews);
        }

        return dto;
    }
}
