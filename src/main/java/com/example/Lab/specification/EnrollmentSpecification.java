package com.example.Lab.specification;

import com.example.Lab.model.Enrollment;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnrollmentSpecification implements Specification<Enrollment> {

    private final Map<String, String> filters;

    public EnrollmentSpecification(Map<String, String> filters) {
        this.filters = filters;
    }

    @Override
    public Predicate toPredicate(Root<Enrollment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (filters.containsKey("studentId")) {
            try {
                Long id = Long.parseLong(filters.get("studentId"));
                predicates.add(cb.equal(root.get("student").get("id"), id));
            } catch (NumberFormatException ignored) {}
        }

        if (filters.containsKey("courseId")) {
            try {
                Long id = Long.parseLong(filters.get("courseId"));
                predicates.add(cb.equal(root.get("course").get("id"), id));
            } catch (NumberFormatException ignored) {}
        }

        if (filters.containsKey("enrollmentDate")) {
            try {
                LocalDate date = LocalDate.parse(filters.get("enrollmentDate"));
                predicates.add(cb.equal(root.get("enrollmentDate"), date));
            } catch (Exception ignored) {}
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
