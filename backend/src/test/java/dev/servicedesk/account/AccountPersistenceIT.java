package dev.servicedesk.account;

import dev.servicedesk.ServiceDeskApplication;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = ServiceDeskApplication.class)
@Testcontainers
class AccountPersistenceIT {
    @Container static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired AccountService service;
    @Autowired AccountRepository repository;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanAccounts() { repository.deleteAll(); }

    @Test
    void storesRequesterAndReloadsVerifiableHashWithoutPlaintext() {
        String password = "  Preserve these spaces  ";
        var created = service.createRequester(new CreateAccount("  ALEX@EXAMPLE.COM ", "  Alex  ", password));
        var reloaded = repository.findByEmail("alex@example.com").orElseThrow();
        assertThat(reloaded.getId()).isEqualTo(created.id());
        assertThat(reloaded.getDisplayName()).isEqualTo("Alex");
        assertThat(reloaded.getRole()).isEqualTo(AccountRole.REQUESTER);
        assertThat(reloaded.isEnabled()).isTrue();
        assertThat(reloaded.getCreatedAt()).isNotNull();
        String storedHash = jdbc.queryForObject("select password_hash from accounts where id = ?", String.class, created.id());
        assertThat(storedHash).startsWith("{bcrypt}").isNotEqualTo(password);
        assertThat(encoder.matches(password, storedHash)).isTrue();
        assertThat(encoder.matches(password.strip(), storedHash)).isFalse();
    }

    @Test
    void databaseRejectsDuplicateEvenWhenServicePrecheckIsBypassed() {
        String hash = "{bcrypt}" + "x".repeat(60);
        repository.saveAndFlush(new Account("same@example.com", "Alex", hash, AccountRole.REQUESTER, Instant.now()));
        assertThatThrownBy(() -> repository.saveAndFlush(new Account("same@example.com", "Other", hash, AccountRole.REQUESTER, Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void databaseRejectsNonCanonicalEmail() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Account("UPPER@example.com", "Alex", "{bcrypt}" + "x".repeat(60), AccountRole.STAFF, Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repository.count()).isZero();
    }
}
