package payment.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.CreateUserRequest;
import payment.system.app.dto.UserResponse;
import payment.system.app.entity.Role;
import payment.system.app.entity.User;
import payment.system.app.exception.DuplicateUserException;
import payment.system.app.exception.InvalidRoleException;
import payment.system.app.exception.UserNotFoundException;
import payment.system.app.exception.WalletCreationException;
import payment.system.app.facade.WalletFacadeService;
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
    
    private final WalletFacadeService walletFacadeService;

    @Transactional
    public UserResponse createUser(
            CreateUserRequest request) {

        logger.info(
                "User creation started for email={}",
                request.getEmail());

        if (userRepository.existsByEmail(
                request.getEmail())) {

            logger.warn(
                    "Duplicate email detected: {}",
                    request.getEmail());

            throw new DuplicateUserException(
                    request.getEmail());
        }

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

        User savedUser =
                userRepository.save(user);

        logger.info(
                "User persisted successfully with id={}",
                savedUser.getId());

        try {

            walletFacadeService
                    .createWallet(savedUser.getId());

            logger.info(
                    "Wallet created successfully for userId={}",
                    savedUser.getId());

        } catch (Exception ex) {

            logger.error(
                    "Wallet creation failed for userId={}",
                    savedUser.getId(),
                    ex);

            throw new WalletCreationException(
                    "Failed to create wallet for userId="
                            + savedUser.getId(),
                    ex);
        }

        logger.info(
                "User creation completed successfully for userId={}",
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

    public UserResponse getUserById(
            Long id) {

        logger.debug(
                "Fetching user for userId={}",
                id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn(
                            "User not found for userId={}",
                            id);

                    return new UserNotFoundException(id);
                });

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

    private Set<Role> validateRoles(
            Set<String> roleNames) {

        if (roleNames == null
                || roleNames.isEmpty()) {

            logger.warn(
                    "Role validation failed: empty roles");

            throw new InvalidRoleException(
                    "At least one role is required");
        }

        Set<Role> roles =
                roleRepository.findByNameIn(roleNames);

        if (roles.size() != roleNames.size()) {

            logger.warn(
                    "Invalid roles detected: {}",
                    roleNames);

            throw new InvalidRoleException(
                    "One or more roles are invalid");
        }

        logger.debug(
                "Roles validated successfully: {}",
                roleNames);

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