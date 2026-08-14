package payment.system.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import payment.system.app.entity.Permission;
import payment.system.app.entity.Role;
import payment.system.app.repository.PermissionRepository;
import payment.system.app.repository.RoleRepository;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class SecurityDataInitializer {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Bean
    CommandLineRunner initializeSecurityData() {

        return args -> {

            // =========================
            // Permissions
            // =========================

            Permission viewUser =
                    createPermission("VIEW_USER");

            Permission updateUser =
                    createPermission("UPDATE_USER");

            Permission deleteUser =
                    createPermission("DELETE_USER");

            Permission createUser =
                    createPermission("CREATE_USER");

            Permission viewWallet =
                    createPermission("VIEW_WALLET");

            Permission creditWallet =
                    createPermission("CREDIT_WALLET");

            Permission debitWallet =
                    createPermission("DEBIT_WALLET");

            Permission createPayment =
                    createPermission("CREATE_PAYMENT");

            Permission viewPayment =
                    createPermission("VIEW_PAYMENT");

            Permission refundPayment =
                    createPermission("REFUND_PAYMENT");


            // =========================
            // Roles
            // =========================

            Role userRole =
                    createRole("ROLE_USER");

            Role adminRole =
                    createRole("ROLE_ADMIN");


            // =========================
            // ROLE_USER permissions
            // =========================

            userRole.setPermissions(Set.of(
                    viewUser,
                    updateUser,
                    viewWallet,
                    createPayment,
                    viewPayment
            ));


            // =========================
            // ROLE_ADMIN permissions
            // =========================

            adminRole.setPermissions(Set.of(
                    createUser,
                    viewUser,
                    updateUser,
                    deleteUser,

                    viewWallet,
                    creditWallet,
                    debitWallet,

                    createPayment,
                    viewPayment,
                    refundPayment
            ));


            // Save roles
            roleRepository.save(userRole);
            roleRepository.save(adminRole);
        };
    }


    private Permission createPermission(String name) {

        return permissionRepository
                .findByName(name)
                .orElseGet(() ->
                        permissionRepository.save(
                                new Permission(name)
                        )
                );
    }


    private Role createRole(String name) {

        return roleRepository
                .findByName(name)
                .orElseGet(() ->
                        new Role(name)
                );
    }
}