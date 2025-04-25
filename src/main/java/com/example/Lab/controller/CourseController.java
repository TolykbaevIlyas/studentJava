package com.example.Lab.controller;

import com.example.Lab.model.Course;
import com.example.Lab.repository.CourseRepository;
import com.example.Lab.specification.CourseSpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // Эндпоинт для получения курсов с пагинацией и сортировкой
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCourses(
            @RequestParam Map<String, String> allParams
    ) {
        try {
            // Извлекаем параметры пагинации и сортировки
            int page = allParams.containsKey("page") ? Integer.parseInt(allParams.get("page")) : 0;
            int size = allParams.containsKey("size") ? Integer.parseInt(allParams.get("size")) : 10;
            String sortField = allParams.getOrDefault("sortField", "title");
            String sortDirection = allParams.getOrDefault("sortDirection", "asc");

            // Удаляем параметры пагинации и сортировки, оставляя только фильтры
            allParams.remove("page");
            allParams.remove("size");
            allParams.remove("sortField");
            allParams.remove("sortDirection");

            // Создаем спецификацию для фильтрации курсов
            Specification<Course> spec = new CourseSpecification(allParams);

            // Создаем объект сортировки и пагинации
            Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Course> coursePage = courseRepository.findAll(spec, pageable);

            // Формируем ответ с пагинацией и информацией о примененных фильтрах
            Map<String, Object> response = new HashMap<>();
            response.put("content", coursePage.getContent());
            response.put("page", coursePage.getNumber());
            response.put("size", coursePage.getSize());
            response.put("totalElements", coursePage.getTotalElements());
            response.put("totalPages", coursePage.getTotalPages());
            response.put("filtersApplied", allParams);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении курсов"));
        }
    }

    // Остальные методы оставляем без изменений
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + id));
    }

    @GetMapping("/{id}/students")
    public List<?> getStudentsByCourse(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + id));
        // Преобразование списка студентов можно оформить через DTO, как было реализовано ранее
        return course.getEnrollments().stream()
                .map(enrollment -> {
                    Map<String, Object> studentDto = new HashMap<>();
                    studentDto.put("id", enrollment.getStudent().getId());
                    studentDto.put("firstName", enrollment.getStudent().getFirstName());
                    studentDto.put("lastName", enrollment.getStudent().getLastName());
                    studentDto.put("email", enrollment.getStudent().getEmail());
                    return studentDto;
                })
                .toList();
    }

    // Создание, обновление и удаление курса
    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseRepository.save(course);
    }

    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course updatedCourse) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + id));
        course.setName(updatedCourse.getName());
        course.setDescription(updatedCourse.getDescription());
        return courseRepository.save(course);
    }

    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Long id) {
        courseRepository.deleteById(id);
    }
}
