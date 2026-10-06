package dev.servicedesk.request;

import dev.servicedesk.ServiceDeskApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ServiceDeskApplication.class)
@Testcontainers
class RequestApiIT {
    @Container static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired WebApplicationContext context;
    @Autowired RequestRepository repository;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createsReadsUpdatesAndFiltersAgainstPostgres() throws Exception {
        var created = mvc.perform(post("/api/requests").contentType("application/json")
                .content("""
                {"title":"Printer 100%","description":"Paper jam","priority":"HIGH"}"""))
                .andExpect(status().isCreated()).andReturn();
        String location = created.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NEW"));
        mvc.perform(patch(location + "/status").contentType("application/json")
                .content("""
                {"status":"RESOLVED"}"""))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVED"));
        mvc.perform(get("/api/requests").param("status", "RESOLVED").param("search", "100%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/requests").param("status", "NEW"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        // Read in a separate repository transaction: status changes must be persisted.
        assertThat(repository.findAll().getFirst().getStatus()).isEqualTo(RequestStatus.RESOLVED);
    }

    @Test
    void rejectsInvalidPagination() throws Exception {
        mvc.perform(get("/api/requests").param("size", "101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/requests").param("page", "-1")).andExpect(status().isBadRequest());
    }
}
