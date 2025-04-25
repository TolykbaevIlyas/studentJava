package com.example.Lab.controller;

import com.example.Lab.model.Student;
import com.example.Lab.repository.StudentRepository;
import com.example.Lab.specification.StudentSpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Эндпоинт для преподавателя: получение всех студентов с поддержкой пагинации и сортировки
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStudents(
            @RequestParam Map<String, String> allParams
    ) {
        try {
            // Извлекаем параметры для пагинации и сортировки и удаляем их из мапы фильтров
            int page = allParams.containsKey("page") ? Integer.parseInt(allParams.get("page")) : 0;
            int size = allParams.containsKey("size") ? Integer.parseInt(allParams.get("size")) : 10;
            String sortField = allParams.getOrDefault("sortField", "firstName");
            String sortDirection = allParams.getOrDefault("sortDirection", "asc");

            // Удаляем параметры пагинации и сортировки, чтобы в спецификации остались только фильтры
            allParams.remove("page");
            allParams.remove("size");
            allParams.remove("sortField");
            allParams.remove("sortDirection");

            // Создаём спецификацию по оставшимся фильтрам
            Specification<Student> spec = new StudentSpecification(allParams);

            Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Student> studentPage = studentRepository.findAll(spec, pageable);

            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("content", studentPage.getContent());
            response.put("page", studentPage.getNumber());
            response.put("size", studentPage.getSize());
            response.put("totalElements", studentPage.getTotalElements());
            response.put("totalPages", studentPage.getTotalPages());
            response.put("filtersApplied", allParams);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении студентов"));
        }
    }

    // Остальные методы оставляем без изменений
    @GetMapping("/me")
    public Student getCurrentStudent(Principal principal) {
        return studentRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Student not found for: " + principal.getName()));
    }

    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentRepository.save(student);
    }

    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Long id, @RequestBody Student updatedStudent) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id " + id));
        student.setFirstName(updatedStudent.getFirstName());
        student.setLastName(updatedStudent.getLastName());
        student.setDob(updatedStudent.getDob());
        student.setEmail(updatedStudent.getEmail());
        return studentRepository.save(student);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
    }
}
