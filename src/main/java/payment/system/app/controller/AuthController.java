package payment.system.app.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import payment.system.app.jwt.Utility.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil) {

        if (authenticationManager == null) {

            logger.error("AuthenticationManager bean is null");

            throw new IllegalArgumentException(
                    "AuthenticationManager cannot be null");
        }

        if (jwtUtil == null) {

            logger.error("JwtUtil bean is null");

            throw new IllegalArgumentException(
                    "JwtUtil cannot be null");
        }

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;

        logger.info("AuthController initialized successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> request) {

        logger.info("Login request received");

        String email = request.get("email");
        String password = request.get("password");

        if (email == null || email.isBlank()) {

            logger.error("Email is missing in login request");

            throw new IllegalArgumentException(
                    "Email is required");
        }

        if (password == null || password.isBlank()) {

            logger.error(
                    "Password is missing for email : {}",
                    email);

            throw new IllegalArgumentException(
                    "Password is required");
        }

        logger.info(
                "Authenticating user with email : {}",
                email);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password));

        logger.info(
                "User authenticated successfully : {}",
                email);

        String token = jwtUtil.generateToken(email);

        if (token == null || token.isBlank()) {

            logger.error(
                    "JWT token generation failed for user : {}",
                    email);

            throw new RuntimeException(
                    "Failed to generate JWT token");
        }

        logger.info(
                "JWT token generated successfully for user : {}",
                email);

        return ResponseEntity.ok(
                Map.of("token", token));
    }
}