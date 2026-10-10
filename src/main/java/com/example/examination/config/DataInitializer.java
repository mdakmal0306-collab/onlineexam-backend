package com.example.examination.config;

import com.example.examination.entity.Exam;
import com.example.examination.entity.ExamAttempt;
import com.example.examination.entity.Answer;
import com.example.examination.entity.Question;
import com.example.examination.entity.Result;
import com.example.examination.entity.User;
import com.example.examination.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final AnswerRepository answerRepository;
    private final ResultRepository resultRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ExamRepository examRepository,
                           QuestionRepository questionRepository,
                           ExamAttemptRepository examAttemptRepository,
                           AnswerRepository answerRepository,
                           ResultRepository resultRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.answerRepository = answerRepository;
        this.resultRepository = resultRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            initUsers();
            initExamsAndQuestions();
        } catch (Exception ex) {
            logger.warn("Data initialization encountered an issue: {}", ex.getMessage());
        }
    }

    private void initUsers() {
        userRepository.findByEmail("admin@example.com").ifPresentOrElse(
            admin -> {
                if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(admin);
                    logger.info("Admin password refreshed: admin@example.com / admin123");
                }
            },
            () -> {
                User admin = new User();
                admin.setName("System Administrator");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                admin.setPhone("9988776655");
                userRepository.save(admin);
                logger.info("Default Admin account created: admin@example.com / admin123");
            }
        );

        userRepository.findByEmail("student@example.com").ifPresentOrElse(
            student -> {
                if (!passwordEncoder.matches("student123", student.getPassword())) {
                    student.setPassword(passwordEncoder.encode("student123"));
                    userRepository.save(student);
                    logger.info("Student password refreshed: student@example.com / student123");
                }
            },
            () -> {
                User student = new User();
                student.setName("Demo Student");
                student.setStudentId("STU001");
                student.setEmail("student@example.com");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRole("ROLE_STUDENT");
                student.setPhone("9876543210");
                userRepository.save(student);
                logger.info("Default Student account created: student@example.com / student123");
            }
        );
    }

    private void initExamsAndQuestions() {
        if (examRepository.count() == 0) {
            // Exam 1
            Exam exam1 = new Exam();
            exam1.setTitle("Java Full Stack Fundamentals");
            exam1.setDescription("Comprehensive assessment of Core Java, OOPs concepts, Collections framework, and Spring fundamentals.");
            exam1.setSubject("Java Programming");
            exam1.setExamDate(LocalDate.now());
            exam1.setStartTime(LocalTime.of(10, 0));
            exam1.setDuration(30);
            exam1.setTotalMarks(25);
            exam1.setPassingMarks(10);
            exam1.setStatus("ACTIVE");
            exam1 = examRepository.save(exam1);

            createQuestion(exam1, "Which keyword is used to inherit a class in Java?",
                    "implements", "extends", "inherits", "instanceof", "B", 5);
            createQuestion(exam1, "Which data structure in Java does not allow duplicate elements?",
                    "List", "ArrayList", "Set", "Vector", "C", 5);
            createQuestion(exam1, "What is the default value of a local variable in Java?",
                    "0", "null", "Garbage value", "No default value (Must be initialized)", "D", 5);
            createQuestion(exam1, "Which annotation in Spring Boot is used to mark a class as a REST Controller?",
                    "@Controller", "@RestController", "@Service", "@Component", "B", 5);
            createQuestion(exam1, "Which of the following is NOT an access modifier in Java?",
                    "private", "protected", "public", "friendly", "D", 5);

            // Exam 2
            Exam exam2 = new Exam();
            exam2.setTitle("Database Management Systems (SQL)");
            exam2.setDescription("Standard examination evaluating relational databases, normalization, SQL queries, and transaction management.");
            exam2.setSubject("DBMS");
            exam2.setExamDate(LocalDate.now());
            exam2.setStartTime(LocalTime.of(14, 0));
            exam2.setDuration(25);
            exam2.setTotalMarks(20);
            exam2.setPassingMarks(8);
            exam2.setStatus("ACTIVE");
            exam2 = examRepository.save(exam2);

            createQuestion(exam2, "Which SQL clause is used to filter records in a query?",
                    "ORDER BY", "WHERE", "GROUP BY", "HAVING", "B", 4);
            createQuestion(exam2, "What does ACID stand for in database transaction management?",
                    "Atomicity, Consistency, Isolation, Durability", "Accuracy, Concurrency, Isolation, Durability", "Atomicity, Compatibility, Integrity, Durability", "Availability, Consistency, Integrity, Dependency", "A", 4);
            createQuestion(exam2, "Which key uniquely identifies each record in a relational database table?",
                    "Foreign Key", "Primary Key", "Candidate Key", "Composite Key", "B", 4);
            createQuestion(exam2, "Which normal form ensures that all non-key attributes are fully functionally dependent on the primary key?",
                    "1NF", "2NF", "3NF", "BCNF", "B", 4);
            createQuestion(exam2, "Which SQL statement is used to remove all rows from a table without logging individual row deletions?",
                    "DELETE", "DROP", "TRUNCATE", "REMOVE", "C", 4);

            // Exam 3
            Exam exam3 = new Exam();
            exam3.setTitle("Web Development & React.js");
            exam3.setDescription("Intermediate examination covering modern ES6+, React Hooks, component lifecycle, and RESTful API integration.");
            exam3.setSubject("Web Technologies");
            exam3.setExamDate(LocalDate.now().plusDays(2));
            exam3.setStartTime(LocalTime.of(9, 0));
            exam3.setDuration(20);
            exam3.setTotalMarks(15);
            exam3.setPassingMarks(6);
            exam3.setStatus("UPCOMING");
            exam3 = examRepository.save(exam3);

            createQuestion(exam3, "Which React hook is used to perform side effects in functional components?",
                    "useEffect", "useState", "useContext", "useReducer", "A", 5);
            createQuestion(exam3, "What is the virtual DOM in React?",
                    "A direct copy of the HTML file", "A browser extension", "An in-memory lightweight representation of the real DOM", "A CSS preprocessor", "C", 5);
            createQuestion(exam3, "How can you pass data from a parent component to a child component in React?",
                    "State", "Props", "Redux store only", "Local Storage only", "B", 5);

            logger.info("Sample examinations and questions initialized successfully.");
        }
    }

    private void createQuestion(Exam exam, String text, String a, String b, String c, String d, String correct, int marks) {
        Question q = new Question();
        q.setExam(exam);
        q.setQuestionText(text);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectAnswer(correct);
        q.setMarks(marks);
        questionRepository.save(q);
    }
}
