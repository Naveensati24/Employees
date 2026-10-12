package com.example.employees.dto;

import com.example.employees.entity.EmployeeStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @NotBlank @Email @Size(max = 120) String email,
        @Pattern(regexp = "^$|^[+0-9 ()-]{7,20}$", message = "must be a valid phone number") String phone,
        @NotBlank @Size(max = 50) String department,
        @NotBlank @Size(max = 80) String jobTitle,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal salary,
        @NotNull @PastOrPresent LocalDate hireDate,
        EmployeeStatus status
) {
}
