package dev.servicedesk.account;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository repository;
    private final PasswordEncoder encoder;
    private final Validator validator;
    private final Clock clock;

    public AccountService(AccountRepository repository, PasswordEncoder encoder, Validator validator, Clock clock) {
        this.repository = repository;
        this.encoder = encoder;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional
    public AccountResponse createRequester(CreateAccount input) {
        var violations = validator.validate(input);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        String password = input.password();
        if (password.isBlank() || password.codePointCount(0, password.length()) < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must contain at least 12 characters and at most 72 UTF-8 bytes.");
        }
        if (repository.existsByEmail(input.email())) throw new AccountAlreadyExistsException();
        var account = new Account(input.email(), input.displayName(), encoder.encode(password),
                AccountRole.REQUESTER, clock.instant());
        // PostgreSQL's unique constraint remains authoritative for concurrent creation.
        return AccountResponse.from(repository.saveAndFlush(account));
    }
}
