package com.example.employees.dto;

import java.math.BigDecimal;
import java.util.List;

public record EmployeeStats(
        long totalEmployees,
        long active,
        long onLeave,
        long terminated,
        BigDecimal averageSalary,
        List<DepartmentStat> departments
) {
}
