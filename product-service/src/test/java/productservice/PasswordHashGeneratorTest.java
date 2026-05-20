package productservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Run manually to generate a BCrypt hash for ADMIN_PASSWORD_HASH:
 * mvn test -Dtest=PasswordHashGeneratorTest#generateHash
 */
class PasswordHashGeneratorTest {

    @Test
    @Disabled("Utility — run explicitly to print a BCrypt hash")
    void generateHash() {
        String raw = System.getenv().getOrDefault("NEW_ADMIN_PASSWORD", "change-me");
        String hash = new BCryptPasswordEncoder().encode(raw);
        System.out.println("ADMIN_PASSWORD_HASH=" + hash);
    }
}
