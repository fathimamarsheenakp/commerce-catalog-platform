package productservice;

import org.junit.jupiter.api.Test;
import productservice.security.JwtUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void shouldGenerateAndReadToken() {

        String token = jwtUtil.generateToken("admin");

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        assertThat(username).isEqualTo("admin");
        assertThat(role).isEqualTo("ADMIN");
    }
}