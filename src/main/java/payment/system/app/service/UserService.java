package payment.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.CreateUserRequest;
import payment.system.app.dto.UserResponse;
import payment.system.app.entity.Role;
import payment.system.app.entity.User;
import payment.system.app.repository.RoleRepository;
import payment.system.app.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Create User
     */
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Set<Role> roles = validateRoles(request.getRoles());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    /**
     * Get All Users
     */
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get User By Id
     */
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    /**
     * Update User
     */
    public UserResponse updateUser(Long id,
                                   CreateUserRequest request) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Set<Role> roles = validateRoles(request.getRoles());

        existingUser.setName(request.getName());
        existingUser.setEmail(request.getEmail());
        existingUser.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        existingUser.setRoles(roles);

        User updatedUser = userRepository.save(existingUser);

        return mapToResponse(updatedUser);
    }

    /**
     * Delete User
     */
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        userRepository.delete(user);
    }

    /**
     * Validate Roles
     */
    private Set<Role> validateRoles(Set<String> roleNames) {

        Set<Role> roles = roleRepository.findByNameIn(roleNames);

        if (roles.isEmpty()) {
            throw new RuntimeException("Invalid roles provided");
        }

        if (roles.size() != roleNames.size()) {
            throw new RuntimeException("One or more roles are not present in DB");
        }

        return roles;
    }

    /**
     * Convert Entity -> DTO
     */
    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet())
                )
                .build();
    }
}