package payment.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Create User
     */
    public UserResponse createUser(
            CreateUserRequest request) {

        logger.info("Create user service started");

        if (request == null) {

            logger.error(
                    "CreateUserRequest is null");

            throw new IllegalArgumentException(
                    "User request cannot be null");
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {

            logger.error(
                    "User email is null or empty");

            throw new IllegalArgumentException(
                    "User email cannot be null or empty");
        }

        logger.info(
                "Checking duplicate email : {}",
                request.getEmail());

        if (userRepository.existsByEmail(
                request.getEmail())) {

            logger.error(
                    "Email already registered : {}",
                    request.getEmail());

            throw new IllegalArgumentException(
                    "Email already registered");
        }

        logger.info(
                "Validating roles for user : {}",
                request.getEmail());

        Set<Role> roles =
                validateRoles(request.getRoles());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()))
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .build();

        logger.info(
                "Saving user with email : {}",
                request.getEmail());

        User savedUser =
                userRepository.save(user);

        if (savedUser == null) {

            logger.error(
                    "Failed to save user with email : {}",
                    request.getEmail());

            throw new RuntimeException(
                    "Failed to create user");
        }

        logger.info(
                "User created successfully with id : {}",
                savedUser.getId());

        return mapToResponse(savedUser);
    }

    /**
     * Get All Users
     */
    public List<UserResponse> getAllUsers() {

        logger.info("Fetching all users");

        List<User> users =
                userRepository.findAll();

        logger.info(
                "Total users fetched : {}",
                users.size());

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get User By Id
     */
    public UserResponse getUserById(
            Long id) {

        logger.info(
                "Fetching user by id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid user id : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid user id is required");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> {

                    logger.error(
                            "User not found with id : {}",
                            id);

                    return new IllegalArgumentException(
                            "User not found with id : "
                                    + id);
                });

        logger.info(
                "User fetched successfully with id : {}",
                id);

        return mapToResponse(user);
    }

    /**
     * Update User
     */
    public UserResponse updateUser(
            Long id,
            CreateUserRequest request) {

        logger.info(
                "Update user service started for id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid user id : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid user id is required");
        }

        if (request == null) {

            logger.error(
                    "Update user request is null");

            throw new IllegalArgumentException(
                    "Update user request cannot be null");
        }

        User existingUser =
                userRepository.findById(id)
                        .orElseThrow(() -> {

                            logger.error(
                                    "User not found with id : {}",
                                    id);

                            return new IllegalArgumentException(
                                    "User not found with id : "
                                            + id);
                        });

        logger.info(
                "Validating roles for user update with id : {}",
                id);

        Set<Role> roles =
                validateRoles(request.getRoles());

        existingUser.setName(request.getName());

        existingUser.setEmail(request.getEmail());

        existingUser.setPassword(
                passwordEncoder.encode(
                        request.getPassword())
        );

        existingUser.setRoles(roles);

        logger.info(
                "Saving updated user with id : {}",
                id);

        User updatedUser =
                userRepository.save(existingUser);

        if (updatedUser == null) {

            logger.error(
                    "Failed to update user with id : {}",
                    id);

            throw new RuntimeException(
                    "Failed to update user");
        }

        logger.info(
                "User updated successfully with id : {}",
                id);

        return mapToResponse(updatedUser);
    }

    /**
     * Delete User
     */
    public void deleteUser(
            Long id) {

        logger.info(
                "Delete user service started for id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid user id : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid user id is required");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> {

                    logger.error(
                            "User not found with id : {}",
                            id);

                    return new IllegalArgumentException(
                            "User not found with id : "
                                    + id);
                });

        logger.info(
                "Deleting user with id : {}",
                id);

        userRepository.delete(user);

        logger.info(
                "User deleted successfully with id : {}",
                id);
    }

    /**
     * Validate Roles
     */
    private Set<Role> validateRoles(
            Set<String> roleNames) {

        logger.info("Validating roles");

        if (roleNames == null
                || roleNames.isEmpty()) {

            logger.error(
                    "Role names are null or empty");

            throw new IllegalArgumentException(
                    "At least one role is required");
        }

        Set<Role> roles =
                roleRepository.findByNameIn(roleNames);

        if (roles.isEmpty()) {

            logger.error(
                    "Invalid roles provided : {}",
                    roleNames);

            throw new IllegalArgumentException(
                    "Invalid roles provided");
        }

        if (roles.size() != roleNames.size()) {

            logger.error(
                    "One or more roles are not present in DB : {}",
                    roleNames);

            throw new IllegalArgumentException(
                    "One or more roles are not present in DB");
        }

        logger.info(
                "Roles validated successfully");

        return roles;
    }

    /**
     * Convert Entity -> DTO
     */
    private UserResponse mapToResponse(
            User user) {

        if (user == null) {

            logger.error(
                    "User entity is null while mapping");

            throw new IllegalArgumentException(
                    "User cannot be null");
        }

        logger.info(
                "Mapping user entity to response DTO for user id : {}",
                user.getId());

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