package com.example.employees.service;

import com.example.employees.dto.EmployeeRequest;
import com.example.employees.dto.EmployeeResponse;
import com.example.employees.dto.EmployeeStats;
import com.example.employees.entity.Employee;
import com.example.employees.entity.EmployeeStatus;
import com.example.employees.exception.DuplicateResourceException;
import com.example.employees.exception.ResourceNotFoundException;
import com.example.employees.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.example.employees.repository.EmployeeSpecifications.*;

@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeServiceImpl(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An employee with email " + request.email() + " already exists");
        }
        Employee employee = new Employee();
        apply(employee, request);
        return EmployeeResponse.from(repository.save(employee));
    }

    @Override
    public Page<EmployeeResponse> searchEmployees(String search, String department, EmployeeStatus status,
                                                  Pageable pageable) {
        Specification<Employee> spec = Specification.allOf(
                matchesSearch(search), hasDepartment(department), hasStatus(status));
        return repository.findAll(spec, pageable).map(EmployeeResponse::from);
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        return EmployeeResponse.from(findOrThrow(id));
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee existing = findOrThrow(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("An employee with email " + request.email() + " already exists");
        }
        apply(existing, request);
        return EmployeeResponse.from(repository.save(existing));
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        repository.delete(findOrThrow(id));
    }

    @Override
    public EmployeeStats getStats() {
        return new EmployeeStats(
                repository.count(),
                repository.countByStatus(EmployeeStatus.ACTIVE),
                repository.countByStatus(EmployeeStatus.ON_LEAVE),
                repository.countByStatus(EmployeeStatus.TERMINATED),
                repository.averageActiveSalary().setScale(2, RoundingMode.HALF_UP),
                repository.departmentStats());
    }

    @Override
    public List<String> getDepartments() {
        return repository.findDistinctDepartments();
    }

    private Employee findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + id));
    }

    private static void apply(Employee e, EmployeeRequest r) {
        e.setFirstName(r.firstName().trim());
        e.setLastName(r.lastName().trim());
        e.setEmail(r.email().trim().toLowerCase());
        e.setPhone(r.phone() == null || r.phone().isBlank() ? null : r.phone().trim());
        e.setDepartment(r.department().trim());
        e.setJobTitle(r.jobTitle().trim());
        e.setSalary(r.salary().setScale(2, RoundingMode.HALF_UP));
        e.setHireDate(r.hireDate());
        e.setStatus(r.status() != null ? r.status() : EmployeeStatus.ACTIVE);
    }
}
