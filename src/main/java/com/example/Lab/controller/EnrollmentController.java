package com.example.Lab.controller;

import com.example.Lab.model.Enrollment;
import com.example.Lab.service.EmailService;
import com.example.Lab.model.Student;
import com.example.Lab.model.Course;
import com.example.Lab.repository.EnrollmentRepository;
import com.example.Lab.repository.StudentRepository;
import com.example.Lab.repository.CourseRepository;
import com.example.Lab.specification.EnrollmentSpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enrollments")
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    private final EmailService emailService;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentController(EnrollmentRepository enrollmentRepository,
                                StudentRepository studentRepository,
                                CourseRepository courseRepository,
                                EmailService emailService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.emailService = emailService;
    }

    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEnrollments(
            @RequestParam Map<String, String> allParams
    ) {
        try {
            // Извлекаем параметры пагинации и сортировки
            int page = allParams.containsKey("page") ? Integer.parseInt(allParams.get("page")) : 0;
            int size = allParams.containsKey("size") ? Integer.parseInt(allParams.get("size")) : 10;
            String sortField = allParams.getOrDefault("sortField", "id");
            String sortDirection = allParams.getOrDefault("sortDirection", "asc");

            // Удаляем параметры пагинации и сортировки, оставляя только фильтры
            allParams.remove("page");
            allParams.remove("size");
            allParams.remove("sortField");
            allParams.remove("sortDirection");

            // Создаем спецификацию для зачислений
            Specification<Enrollment> spec = new EnrollmentSpecification(allParams);

            // Создаем объект сортировки и пагинации
            Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Enrollment> enrollmentPage = enrollmentRepository.findAll(spec, pageable);

            // Формируем ответ с информацией о пагинации и примененных фильтрах
            Map<String, Object> response = new HashMap<>();
            response.put("content", enrollmentPage.getContent());
            response.put("page", enrollmentPage.getNumber());
            response.put("size", enrollmentPage.getSize());
            response.put("totalElements", enrollmentPage.getTotalElements());
            response.put("totalPages", enrollmentPage.getTotalPages());
            response.put("filtersApplied", allParams);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении зачислений"));
        }
    }

    // Эндпоинт для студента: получение своих зачислений
    @GetMapping("/me")
    public List<Enrollment> getCurrentStudentEnrollments(Principal principal) {
        Student student = studentRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Student not found for: " + principal.getName()));
        return enrollmentRepository.findAll().stream()
                .filter(enrollment -> enrollment.getStudent().getId().equals(student.getId()))
                .collect(Collectors.toList());
    }


    // Создание нового зачисления (только преподаватель или админ)
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @PostMapping
    public Enrollment createEnrollment(@RequestBody Map<String, Long> dto) {
        Long studentId = dto.get("studentId");
        Long courseId = dto.get("courseId");

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + courseId));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 🚀 Отправка письма после успешного сохранения
        emailService.sendEnrollmentConfirmation(
                student.getEmail(),
                student.getFullName(),         // Или student.getFirstName() + " " + student.getLastName()
                course.getTitle(),
                java.time.LocalDate.now().toString()
        );

        return savedEnrollment;
    }

    // Обновление зачисления (только преподаватель или админ)
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Enrollment updateEnrollment(@PathVariable Long id, @RequestBody Map<String, Long> dto) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id " + id));
        Long studentId = dto.get("studentId");
        Long courseId = dto.get("courseId");

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + courseId));

        enrollment.setStudent(student);
        enrollment.setCourse(course);
        return enrollmentRepository.save(enrollment);
    }

    // Удаление зачисления (только преподаватель или админ)
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteEnrollment(@PathVariable Long id) {
        enrollmentRepository.deleteById(id);
    }
}
