package com.example.employees.controller;

import com.example.employees.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.seed-data=false")
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EmployeeRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private static String body(String first, String email, String dept, String status) {
        return """
                {"firstName":"%s","lastName":"Tester","email":"%s","phone":"+1 555 0100",
                 "department":"%s","jobTitle":"Engineer","salary":95000,
                 "hireDate":"2023-05-01","status":"%s"}
                """.formatted(first, email, dept, status);
    }

    private long create(String first, String email, String dept, String status) throws Exception {
        String json = mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(body(first, email, dept, status)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/employees/")))
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(json.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        long id = create("Ada", "ada@example.com", "Engineering", "ACTIVE");

        mvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Ada", "ada@example.com", "Research", "ON_LEAVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Research"))
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));

        mvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void validationErrorsAreReportedPerField() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"email\":\"not-an-email\",\"salary\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.salary").exists())
                .andExpect(jsonPath("$.errors.hireDate").exists());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        create("Ada", "ada@example.com", "Engineering", "ACTIVE");
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Other", "ADA@example.com", "Sales", "ACTIVE")))
                .andExpect(status().isConflict());
    }

    @Test
    void searchFiltersSortsAndPaginates() throws Exception {
        create("Ada", "ada@example.com", "Engineering", "ACTIVE");
        create("Grace", "grace@example.com", "Engineering", "ON_LEAVE");
        create("Linus", "linus@example.com", "Sales", "ACTIVE");

        mvc.perform(get("/api/employees").param("department", "engineering")
                        .param("sortBy", "firstName").param("direction", "desc"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("Grace"));

        mvc.perform(get("/api/employees").param("search", "LIN"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Linus"));

        mvc.perform(get("/api/employees").param("status", "ACTIVE").param("size", "1"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void statsAggregateByStatusAndDepartment() throws Exception {
        create("Ada", "ada@example.com", "Engineering", "ACTIVE");
        create("Grace", "grace@example.com", "Engineering", "ON_LEAVE");
        create("Linus", "linus@example.com", "Sales", "TERMINATED");

        mvc.perform(get("/api/employees/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees").value(3))
                .andExpect(jsonPath("$.active").value(1))
                .andExpect(jsonPath("$.onLeave").value(1))
                .andExpect(jsonPath("$.terminated").value(1))
                .andExpect(jsonPath("$.departments", hasSize(1)))
                .andExpect(jsonPath("$.departments[0].department").value("Engineering"))
                .andExpect(jsonPath("$.departments[0].headcount").value(2));

        mvc.perform(get("/api/employees/departments"))
                .andExpect(jsonPath("$", contains("Engineering", "Sales")));
    }
}
