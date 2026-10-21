package dev.servicedesk.account;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(UUID id, String email, String displayName, AccountRole role,
                              boolean enabled, Instant createdAt) {
    static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getEmail(), account.getDisplayName(),
                account.getRole(), account.isEnabled(), account.getCreatedAt());
    }
}
