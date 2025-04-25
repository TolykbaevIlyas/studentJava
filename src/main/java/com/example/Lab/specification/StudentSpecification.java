package com.example.Lab.specification;

import com.example.Lab.model.Student;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentSpecification implements Specification<Student> {

    private final Map<String, String> filters;

    public StudentSpecification(Map<String, String> filters) {
        this.filters = filters;
    }

    @Override
    public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Фильтрация по имени (поиск в firstName и lastName)
        if (filters.containsKey("name") || filters.containsKey("name_like")) {
            String nameValue = filters.containsKey("name") ? filters.get("name") : filters.get("name_like");
            Predicate firstNameLike = cb.like(cb.lower(root.get("firstName")), "%" + nameValue.toLowerCase() + "%");
            Predicate lastNameLike  = cb.like(cb.lower(root.get("lastName")), "%" + nameValue.toLowerCase() + "%");
            predicates.add(cb.or(firstNameLike, lastNameLike));
        }

        // Фильтрация по email с поддержкой частичного совпадения
        if (filters.containsKey("email") || filters.containsKey("email_like")) {
            String emailValue = filters.containsKey("email") ? filters.get("email") : filters.get("email_like");
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + emailValue.toLowerCase() + "%"));
        }

        // Фильтрация по группе
        if (filters.containsKey("group")) {
            String groupValue = filters.get("group");
            predicates.add(cb.equal(root.get("group"), groupValue));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
