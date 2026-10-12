package com.example.employees.service;

import com.example.employees.dto.EmployeeRequest;
import com.example.employees.dto.EmployeeResponse;
import com.example.employees.dto.EmployeeStats;
import com.example.employees.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);
    Page<EmployeeResponse> searchEmployees(String search, String department, EmployeeStatus status, Pageable pageable);
    EmployeeResponse getEmployeeById(Long id);
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    void deleteEmployee(Long id);
    EmployeeStats getStats();
    List<String> getDepartments();
}
