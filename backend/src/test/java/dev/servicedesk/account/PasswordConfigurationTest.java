package dev.servicedesk.account;

import dev.servicedesk.config.PasswordConfiguration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PasswordConfigurationTest {
    @Test
    void productionEncoderSaltsHashesAndMatchesOnlyCorrectPassword() {
        var encoder = new PasswordConfiguration().passwordEncoder();
        String password = "a realistic password phrase";
        String first = encoder.encode(password);
        String second = encoder.encode(password);
        assertThat(first).startsWith("{bcrypt}$2a$12$").isNotEqualTo(second).isNotEqualTo(password);
        assertThat(encoder.matches(password, first)).isTrue();
        assertThat(encoder.matches("wrong password phrase", first)).isFalse();
    }
}
