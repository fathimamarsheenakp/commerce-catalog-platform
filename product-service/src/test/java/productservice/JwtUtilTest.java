package productservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import productservice.security.JwtUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @BeforeEach
    void setSecret() {
        ReflectionTestUtils.setField(
                jwtUtil,
                "secret",
                "my-super-secret-key-my-super-secret-key-123456"
        );
    }

    @Test
    void shouldGenerateAndReadToken() {

        String token = jwtUtil.generateToken("admin", "ADMIN");

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        assertThat(username).isEqualTo("admin");
        assertThat(role).isEqualTo("ADMIN");
    }
}