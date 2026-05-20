package productservice.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtSecretValidator {

    private static final String WEAK_DEFAULT =
            "my-super-secret-key-my-super-secret-key-123456";

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    void validate() {
        if (secret == null || secret.length() < 32) {
            log.warn(
                    "JWT secret is shorter than 32 characters. Set JWT_SECRET to a long random value.");
        }
        if (WEAK_DEFAULT.equals(secret)) {
            log.warn(
                    "Using default JWT secret. Set JWT_SECRET environment variable before any shared deployment.");
        }
    }
}
