package payment.system.app.config;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import payment.system.app.entity.Role;
import payment.system.app.entity.User;
import payment.system.app.repository.RoleRepository;
import payment.system.app.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner createInitialAdmin() {
        return args -> {

            if (adminEmail == null || adminEmail.isBlank()
                    || adminPassword == null || adminPassword.isBlank()) {
                return;
            }

            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }

            Role adminRole = roleRepository
                    .findByName("ROLE_ADMIN")
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "ROLE_ADMIN is not configured"));

            User admin = User.builder()
                    .name("System Administrator")
                    .email(adminEmail.trim().toLowerCase())
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole))
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
        };
    }
}
