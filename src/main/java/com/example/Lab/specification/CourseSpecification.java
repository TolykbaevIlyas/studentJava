package com.example.Lab.specification;

import com.example.Lab.model.Course;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CourseSpecification implements Specification<Course> {

    private final Map<String, String> filters;

    public CourseSpecification(Map<String, String> filters) {
        this.filters = filters;
    }

    @Override
    public Predicate toPredicate(Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (filters.containsKey("title") || filters.containsKey("title_like")) {
            String value = filters.getOrDefault("title", filters.get("title_like"));
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + value.toLowerCase() + "%"));
        }

        if (filters.containsKey("creditHours")) {
            try {
                int credit = Integer.parseInt(filters.get("creditHours"));
                predicates.add(cb.equal(root.get("creditHours"), credit));
            } catch (NumberFormatException ignored) {}
        }

        if (filters.containsKey("instructorName") || filters.containsKey("instructorName_like")) {
            String value = filters.getOrDefault("instructorName", filters.get("instructorName_like"));
            predicates.add(cb.like(cb.lower(root.get("instructorName")), "%" + value.toLowerCase() + "%"));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
