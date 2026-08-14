package payment.system.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import payment.system.app.entity.Role;
import payment.system.app.repository.RoleRepository;

@Configuration
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));
    }
}
