package dev.servicedesk.account;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final ValidatorFactory VALIDATORS = Validation.buildDefaultValidatorFactory();
    private static final Instant NOW = Instant.parse("2026-10-21T12:00:00Z");
    @Mock AccountRepository repository;
    @Mock PasswordEncoder encoder;
    AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(repository, encoder, VALIDATORS.getValidator(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @AfterAll
    static void closeValidator() { VALIDATORS.close(); }

    @Test
    void createsOnlyRequesterAndPersistsHashInsteadOfPassword() {
        String password = "  Keep my spaces  ";
        when(encoder.encode(password)).thenReturn("{bcrypt}encoded-value");
        when(repository.saveAndFlush(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.createRequester(new CreateAccount("  PERSON@EXAMPLE.COM ", "  Alex  ", password));
        var saved = ArgumentCaptor.forClass(Account.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("{bcrypt}encoded-value");
        assertThat(response.email()).isEqualTo("person@example.com");
        assertThat(response.displayName()).isEqualTo("Alex");
        assertThat(response.role()).isEqualTo(AccountRole.REQUESTER);
        assertThat(response.enabled()).isTrue();
        assertThat(response.createdAt()).isEqualTo(NOW);
        assertThat(response.id()).isNotNull();
        verify(encoder).encode(password);
    }

    @Test
    void rejectsDuplicateCanonicalEmailBeforeHashing() {
        when(repository.existsByEmail("person@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.createRequester(new CreateAccount(" PERSON@EXAMPLE.COM ", "Alex", "valid password phrase")))
                .isInstanceOf(AccountAlreadyExistsException.class);
        verifyNoInteractions(encoder);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> service.createRequester(new CreateAccount("not-an-email", "Alex", "valid password phrase")))
                .isInstanceOf(ConstraintViolationException.class);
        verifyNoInteractions(repository, encoder);
    }

    @Test
    void rejectsBlankOrOversizeDisplayName() {
        for (String name : new String[] { "  ", "a".repeat(101) }) {
            assertThatThrownBy(() -> service.createRequester(new CreateAccount("person@example.com", name, "valid password phrase")))
                    .isInstanceOf(ConstraintViolationException.class);
        }
        verifyNoInteractions(repository, encoder);
    }

    @Test
    void rejectsNullPassword() {
        assertThatThrownBy(() -> service.createRequester(new CreateAccount("person@example.com", "Alex", null)))
                .isInstanceOf(ConstraintViolationException.class);
        verifyNoInteractions(repository, encoder);
    }

    @Test
    void rejectsShortBlankOrOverByteLimitPasswords() {
        for (String password : new String[] { "short", " ".repeat(12), "a".repeat(73), "é".repeat(37), "😀".repeat(19) }) {
            assertThatThrownBy(() -> service.createRequester(new CreateAccount("person@example.com", "Alex", password)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        verifyNoInteractions(repository, encoder);
    }

    @Test
    void acceptsExactUtf8ByteLimit() {
        String password = "é".repeat(36);
        when(encoder.encode(password)).thenReturn("{bcrypt}encoded-value");
        when(repository.saveAndFlush(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.createRequester(new CreateAccount("person@example.com", "Alex", password));
        verify(encoder).encode(password);
    }

    @Test
    void commandStringDoesNotRevealCredentials() {
        assertThat(new CreateAccount("person@example.com", "Alex", "private password phrase").toString())
                .doesNotContain("private password phrase", "person@example.com");
    }
}
