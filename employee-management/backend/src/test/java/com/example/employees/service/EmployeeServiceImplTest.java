package com.example.employees.service;

import com.example.employees.dto.EmployeeRequest;
import com.example.employees.dto.EmployeeResponse;
import com.example.employees.entity.Employee;
import com.example.employees.entity.EmployeeStatus;
import com.example.employees.exception.DuplicateResourceException;
import com.example.employees.exception.ResourceNotFoundException;
import com.example.employees.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeServiceImpl service;

    private static EmployeeRequest request(String email) {
        return new EmployeeRequest(" Jane ", "Doe", email, "", "Engineering", "Engineer",
                new BigDecimal("100000"), LocalDate.of(2022, 1, 1), null);
    }

    @Test
    void createNormalizesFieldsAndDefaultsStatus() {
        when(repository.existsByEmailIgnoreCase("Jane@Example.com")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse result = service.createEmployee(request("Jane@Example.com"));

        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.email()).isEqualTo("jane@example.com");
        assertThat(result.phone()).isNull();
        assertThat(result.status()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(result.salary()).isEqualByComparingTo("100000.00");
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createEmployee(request("jane@example.com")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void getMissingEmployeeThrowsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEmployeeById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void updateRejectsEmailOwnedByAnotherEmployee() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Employee()));
        when(repository.existsByEmailIgnoreCaseAndIdNot("taken@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateEmployee(1L, request("taken@example.com")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteMissingEmployeeThrowsNotFound() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteEmployee(7L)).isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).delete(any(Employee.class));
    }
}
