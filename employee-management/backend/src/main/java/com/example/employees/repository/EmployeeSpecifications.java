package com.example.employees.repository;

import com.example.employees.entity.Employee;
import com.example.employees.entity.EmployeeStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    /** Matches the search term against first name, last name, email and job title. */
    public static Specification<Employee> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("jobTitle")), pattern)
            );
        };
    }

    public static Specification<Employee> hasDepartment(String department) {
        return (root, query, cb) -> StringUtils.hasText(department)
                ? cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase())
                : null;
    }

    public static Specification<Employee> hasStatus(EmployeeStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }
}
