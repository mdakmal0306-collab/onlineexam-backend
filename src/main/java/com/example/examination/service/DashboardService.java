package com.example.examination.service;

import com.example.examination.dto.DashboardStatsDTO;
import com.example.examination.dto.ExamDTO;
import com.example.examination.dto.ExamStatDTO;
import com.example.examination.dto.ResultResponseDTO;
import com.example.examination.entity.Exam;
import com.example.examination.entity.Result;
import com.example.examination.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ResultRepository resultRepository;
    private final ExamService examService;
    private final ExamAttemptService examAttemptService;

    public DashboardService(UserRepository userRepository,
                            ExamRepository examRepository,
                            QuestionRepository questionRepository,
                            ExamAttemptRepository examAttemptRepository,
                            ResultRepository resultRepository,
                            ExamService examService,
                            ExamAttemptService examAttemptService) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.resultRepository = resultRepository;
        this.examService = examService;
        this.examAttemptService = examAttemptService;
    }

    public DashboardStatsDTO getAdminDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        stats.setTotalStudents(userRepository.countByRole("ROLE_STUDENT"));
        stats.setTotalExams(examRepository.count());
        stats.setUpcomingExams(examRepository.countByStatus("UPCOMING"));
        stats.setActiveExams(examRepository.countByStatus("ACTIVE"));
        stats.setCompletedExams(examRepository.countByStatus("COMPLETED"));
        stats.setTotalQuestions(questionRepository.count());
        stats.setTotalAttempts(examAttemptRepository.count());
        stats.setTotalResults(resultRepository.count());

        stats.setPassCount(resultRepository.countByResultStatus("PASS"));
        stats.setFailCount(resultRepository.countByResultStatus("FAIL"));

        Double avgPercentage = resultRepository.calculateAveragePercentage();
        stats.setAveragePercentage(avgPercentage != null ? Math.round(avgPercentage * 100.0) / 100.0 : 0.0);

        List<ExamDTO> recentExams = examRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(5)
                .map(examService::mapToDTO)
                .collect(Collectors.toList());
        stats.setRecentExams(recentExams);

        List<ResultResponseDTO> recentResults = resultRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(5)
                .map(examAttemptService::buildResultResponse)
                .collect(Collectors.toList());
        stats.setRecentResults(recentResults);

        return stats;
    }

    public List<ExamStatDTO> getAllExamStatistics() {
        List<Exam> exams = examRepository.findAllByOrderByCreatedAtDesc();
        List<ExamStatDTO> list = new ArrayList<>();

        for (Exam exam : exams) {
            ExamStatDTO stat = new ExamStatDTO();
            stat.setExamId(exam.getId());
            stat.setExamTitle(exam.getTitle());
            stat.setSubject(exam.getSubject());
            stat.setTotalMarks(exam.getTotalMarks());

            List<Result> results = resultRepository.findByExamIdOrderByCreatedAtDesc(exam.getId());
            long attempts = results.size();
            long passed = results.stream().filter(r -> "PASS".equalsIgnoreCase(r.getResultStatus())).count();
            long failed = attempts - passed;

            stat.setTotalAttempts(attempts);
            stat.setPassedCount(passed);
            stat.setFailedCount(failed);
            stat.setPassRate(attempts > 0 ? Math.round(((double) passed / attempts) * 10000.0) / 100.0 : 0.0);

            double avgMarks = results.stream().mapToInt(Result::getObtainedMarks).average().orElse(0.0);
            stat.setAverageMarks(Math.round(avgMarks * 100.0) / 100.0);

            int highest = results.stream().mapToInt(Result::getObtainedMarks).max().orElse(0);
            stat.setHighestMarks(highest);

            list.add(stat);
        }

        return list;
    }

    public ExamStatDTO getExamStatisticById(Long examId) {
        Exam exam = examService.getExamEntityById(examId);
        ExamStatDTO stat = new ExamStatDTO();
        stat.setExamId(exam.getId());
        stat.setExamTitle(exam.getTitle());
        stat.setSubject(exam.getSubject());
        stat.setTotalMarks(exam.getTotalMarks());

        List<Result> results = resultRepository.findByExamIdOrderByCreatedAtDesc(exam.getId());
        long attempts = results.size();
        long passed = results.stream().filter(r -> "PASS".equalsIgnoreCase(r.getResultStatus())).count();
        long failed = attempts - passed;

        stat.setTotalAttempts(attempts);
        stat.setPassedCount(passed);
        stat.setFailedCount(failed);
        stat.setPassRate(attempts > 0 ? Math.round(((double) passed / attempts) * 10000.0) / 100.0 : 0.0);

        double avgMarks = results.stream().mapToInt(Result::getObtainedMarks).average().orElse(0.0);
        stat.setAverageMarks(Math.round(avgMarks * 100.0) / 100.0);

        int highest = results.stream().mapToInt(Result::getObtainedMarks).max().orElse(0);
        stat.setHighestMarks(highest);

        return stat;
    }
}
