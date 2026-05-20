package productservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import productservice.exception.InvalidLoginException;
import productservice.security.JwtUtil;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password-hash}")
    private String adminPasswordHash;

    @Value("${app.user.username}")
    private String userUsername;

    @Value("${app.user.password-hash}")
    private String userPasswordHash;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {

        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            throw new InvalidLoginException("Invalid username or password");
        }

        if (adminUsername.equals(username) && passwordEncoder.matches(password, adminPasswordHash)) {
            String token = jwtUtil.generateToken(username, "ADMIN");
            return Collections.singletonMap("token", token);
        }

        if (userUsername.equals(username)
                && passwordEncoder.matches(password, userPasswordHash)) {

            String token = jwtUtil.generateToken(username, "USER");
            return Collections.singletonMap("token", token);
        }

        throw new InvalidLoginException("Invalid username or password");
    }
}
