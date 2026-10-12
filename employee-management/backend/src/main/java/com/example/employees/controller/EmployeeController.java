package com.example.employees.controller;

import com.example.employees.dto.EmployeeRequest;
import com.example.employees.dto.EmployeeResponse;
import com.example.employees.dto.EmployeeStats;
import com.example.employees.dto.PageResponse;
import com.example.employees.entity.EmployeeStatus;
import com.example.employees.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Manage employee records")
public class EmployeeController {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "firstName", "lastName", "email", "department", "jobTitle", "salary", "hireDate", "status");

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Search employees with filters, sorting and pagination")
    public PageResponse<EmployeeResponse> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        String sortField = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "id";
        Sort sort = Sort.by("desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC, sortField);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);
        return PageResponse.from(employeeService.searchEmployees(search, department, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee by id")
    public EmployeeResponse getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    @Operation(summary = "Create an employee")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employeeService.createEmployee(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee")
    public EmployeeResponse updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an employee")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }

    @GetMapping("/stats")
    @Operation(summary = "Dashboard statistics: headcount by status and department, average salary")
    public EmployeeStats getStats() {
        return employeeService.getStats();
    }

    @GetMapping("/departments")
    @Operation(summary = "List distinct department names")
    public List<String> getDepartments() {
        return employeeService.getDepartments();
    }
}
