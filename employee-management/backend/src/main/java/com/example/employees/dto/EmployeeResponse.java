package com.example.employees.dto;

import com.example.employees.entity.Employee;
import com.example.employees.entity.EmployeeStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String department,
        String jobTitle,
        BigDecimal salary,
        LocalDate hireDate,
        EmployeeStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getPhone(),
                e.getDepartment(), e.getJobTitle(), e.getSalary(), e.getHireDate(), e.getStatus(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
