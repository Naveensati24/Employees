package com.example.employees.repository;

import com.example.employees.dto.DepartmentStat;
import com.example.employees.entity.Employee;
import com.example.employees.entity.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    long countByStatus(EmployeeStatus status);

    @Query("select coalesce(avg(e.salary), 0) from Employee e where e.status <> com.example.employees.entity.EmployeeStatus.TERMINATED")
    BigDecimal averageActiveSalary();

    @Query("""
            select new com.example.employees.dto.DepartmentStat(e.department, count(e), coalesce(avg(e.salary), 0))
            from Employee e
            where e.status <> com.example.employees.entity.EmployeeStatus.TERMINATED
            group by e.department
            order by count(e) desc
            """)
    List<DepartmentStat> departmentStats();

    @Query("select distinct e.department from Employee e order by e.department")
    List<String> findDistinctDepartments();
}
