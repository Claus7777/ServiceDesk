package dev.servicedesk.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Locale;

/** Internal command; account creation is not exposed over HTTP in this milestone. */
public record CreateAccount(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String displayName,
        @NotNull String password) {
    public CreateAccount {
        if (email != null) email = email.strip().toLowerCase(Locale.ROOT);
        if (displayName != null) displayName = displayName.strip();
        // Do not trim or otherwise transform passwords.
    }

    @Override
    public String toString() { return "CreateAccount[credentials redacted]"; }
}
