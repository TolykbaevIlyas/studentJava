package com.example.Lab;

import com.example.Lab.model.Student;
import com.example.Lab.model.Course;
import com.example.Lab.model.Enrollment;
import com.example.Lab.repository.StudentRepository;
import com.example.Lab.repository.CourseRepository;
import com.example.Lab.repository.EnrollmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public DataLoader(StudentRepository studentRepository,
                      CourseRepository courseRepository,
                      EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Создаем студентов с проверкой наличия записи по email
        createStudentIfNotExists("alice@example.com", "Alice", "Smith", "2000-01-01");
        createStudentIfNotExists("bob@example.com", "Bob", "Johnson", "1999-05-15");
        createStudentIfNotExists("ilyas.tolykbaev04@gmail.com", "Ilyas", "Tolykbaev", "2004-06-15");


        // Создаем курсы
        Course course1 = new Course();
        course1.setName("Mathematics");
        course1.setDescription("Математика");
        course1.setCreditHours(10);
        courseRepository.save(course1);

        Course course2 = new Course();
        course2.setName("Physics");
        course2.setDescription("Physics");
        course2.setCreditHours(20);
        courseRepository.save(course2);

        // Создаем зачисления
        // Предполагается, что студенты и курсы созданы корректно
        Student alice = studentRepository.findByEmail("alice@example.com").orElseThrow();
        Student bob = studentRepository.findByEmail("bob@example.com").orElseThrow();

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setStudent(alice);
        enrollment1.setCourse(course1);
        enrollmentRepository.save(enrollment1);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setStudent(bob);
        enrollment2.setCourse(course2);
        enrollmentRepository.save(enrollment2);
    }

    private void createStudentIfNotExists(String email, String firstName, String lastName, String dobStr) {
        studentRepository.findByEmail(email).ifPresentOrElse(
                student -> System.out.println("Student with email " + email + " already exists. Skipping insertion."),
                () -> {
                    Student student = new Student();
                    student.setFirstName(firstName);
                    student.setLastName(lastName);
                    student.setEmail(email);
                    // Преобразование строки в LocalDate для поля dob
                    student.setDob(LocalDate.parse(dobStr));
                    // Установка текущей даты и времени для поля createdAt, если требуется
                    student.setCreatedAt(LocalDateTime.now());

                    studentRepository.save(student);
                    System.out.println("Student with email " + email + " created successfully.");
                }
        );
    }
}
