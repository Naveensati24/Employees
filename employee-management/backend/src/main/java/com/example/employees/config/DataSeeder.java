package com.example.employees.config;

import com.example.employees.entity.Employee;
import com.example.employees.entity.EmployeeStatus;
import com.example.employees.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Seeds sample employees on an empty database so the UI has something to show out of the box. */
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final EmployeeRepository repository;

    public DataSeeder(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        List<Employee> employees = List.of(
                employee("Aarav", "Sharma", "Engineering", "Senior Software Engineer", "145000", "2019-03-11", EmployeeStatus.ACTIVE),
                employee("Priya", "Patel", "Engineering", "Frontend Engineer", "112000", "2021-07-01", EmployeeStatus.ACTIVE),
                employee("Rohan", "Mehta", "Engineering", "DevOps Engineer", "121000", "2020-11-16", EmployeeStatus.ON_LEAVE),
                employee("Sara", "Khan", "Engineering", "Engineering Manager", "168000", "2017-05-22", EmployeeStatus.ACTIVE),
                employee("Emily", "Johnson", "Product", "Product Manager", "138000", "2020-02-03", EmployeeStatus.ACTIVE),
                employee("Daniel", "Lee", "Product", "UX Designer", "104000", "2022-01-10", EmployeeStatus.ACTIVE),
                employee("Ananya", "Iyer", "Sales", "Account Executive", "89000", "2021-09-27", EmployeeStatus.ACTIVE),
                employee("Michael", "Brown", "Sales", "Sales Director", "152000", "2016-08-15", EmployeeStatus.ACTIVE),
                employee("Kavya", "Reddy", "Human Resources", "HR Business Partner", "92000", "2019-12-02", EmployeeStatus.ACTIVE),
                employee("James", "Wilson", "Finance", "Financial Analyst", "97000", "2023-04-17", EmployeeStatus.ACTIVE),
                employee("Neha", "Gupta", "Finance", "Controller", "131000", "2018-06-04", EmployeeStatus.ON_LEAVE),
                employee("Arjun", "Nair", "Marketing", "Growth Marketer", "86000", "2022-10-03", EmployeeStatus.ACTIVE),
                employee("Olivia", "Martinez", "Marketing", "Content Strategist", "78000", "2020-03-30", EmployeeStatus.TERMINATED),
                employee("Vikram", "Singh", "Support", "Support Lead", "74000", "2021-01-18", EmployeeStatus.ACTIVE)
        );
        repository.saveAll(employees);
        log.info("Seeded {} sample employees", employees.size());
    }

    private static Employee employee(String first, String last, String dept, String title, String salary,
                                     String hireDate, EmployeeStatus status) {
        Employee e = new Employee();
        e.setFirstName(first);
        e.setLastName(last);
        e.setEmail((first + "." + last + "@acme.example").toLowerCase());
        e.setPhone("+1 555 01" + String.format("%02d", Math.abs((first + last).hashCode()) % 100));
        e.setDepartment(dept);
        e.setJobTitle(title);
        e.setSalary(new BigDecimal(salary));
        e.setHireDate(LocalDate.parse(hireDate));
        e.setStatus(status);
        return e;
    }
}
