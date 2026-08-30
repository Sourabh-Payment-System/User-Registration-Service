package payment.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.CreateUserRequest;
import payment.system.app.dto.RegisterUserRequest;
import payment.system.app.dto.UserResponse;
import payment.system.app.entity.OutboxEvent;
import payment.system.app.entity.Role;
import payment.system.app.entity.User;
import payment.system.app.enums.OutboxStatus;
import payment.system.app.event.UserRegisteredEvent;
import payment.system.app.exception.DuplicateUserException;
import payment.system.app.exception.InvalidRoleException;
import payment.system.app.exception.UserNotFoundException;
import payment.system.app.repository.OutboxEventRepository;
import payment.system.app.repository.RoleRepository;
import payment.system.app.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Public registration.
     * Role is always USER.
     */
    @Transactional
    public UserResponse registerUser(
            RegisterUserRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException(email);
        }

        Role userRole =
                roleRepository.findByName("USER")
                        .orElseThrow(() ->
                                new InvalidRoleException(
                                        "Default USER role is not configured"
                                )
                        );

        User user =
                User.builder()
                        .name(request.name().trim())
                        .email(email)
                        .password(
                                passwordEncoder.encode(
                                        request.password()
                                )
                        )
                        .roles(Set.of(userRole))
                        .createdAt(LocalDateTime.now())
                        .build();

        User savedUser = userRepository.save(user);

        // Save event in the SAME database transaction
        saveUserRegisteredOutboxEvent(savedUser);

        return mapToResponse(savedUser);
    }

    /**
     * Admin/internal user creation.
     */
    @Transactional
    public UserResponse createUser(
            CreateUserRequest request) {

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException(email);
        }

        Set<Role> roles =
                validateRoles(request.getRoles());

        User user =
                User.builder()
                        .name(request.getName().trim())
                        .email(email)
                        .password(
                                passwordEncoder.encode(
                                        request.getPassword()
                                )
                        )
                        .roles(roles)
                        .createdAt(LocalDateTime.now())
                        .build();

        User savedUser = userRepository.save(user);

        // Save event in the SAME database transaction
        saveUserRegisteredOutboxEvent(savedUser);

        return mapToResponse(savedUser);
    }

    private void saveUserRegisteredOutboxEvent(
            User user) {

        String eventId =
                UUID.randomUUID().toString();

        UserRegisteredEvent event =
                new UserRegisteredEvent(
                        eventId,
                        user.getId(),
                        user.getEmail()
                );

        String payload;

        try {

            payload =
                    objectMapper.writeValueAsString(
                            event
                    );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to serialize "
                            + "UserRegisteredEvent",
                    e
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        OutboxEvent outboxEvent =
                new OutboxEvent();

        outboxEvent.setEventId(eventId);
        outboxEvent.setAggregateType("USER");
        outboxEvent.setAggregateId(user.getId());
        outboxEvent.setEventType("USER_REGISTERED");
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus(
                OutboxStatus.PENDING
        );
        outboxEvent.setCreatedAt(now);
        outboxEvent.setUpdatedAt(now);
        outboxEvent.setRetryCount(0);

        outboxEventRepository.save(outboxEvent);
    }
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(id)
                        );

        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(
            Long id,
            CreateUserRequest request) {

        User existing =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(id)
                        );

        String email =
                normalizeEmail(request.getEmail());

        userRepository.findByEmail(email)
                .filter(other ->
                        !other.getId().equals(id)
                )
                .ifPresent(other -> {
                    throw new DuplicateUserException(email);
                });

        existing.setName(request.getName().trim());
        existing.setEmail(email);
        existing.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        existing.setRoles(
                validateRoles(request.getRoles())
        );

        return mapToResponse(
                userRepository.save(existing)
        );
    }

    @Transactional
    public void deleteUser(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(id)
                        );

        userRepository.delete(user);
    }

    private Set<Role> validateRoles(
            Set<String> roleNames) {

        if (roleNames == null || roleNames.isEmpty()) {
            throw new InvalidRoleException(
                    "At least one role is required"
            );
        }

        Set<Role> roles =
                roleRepository.findByNameIn(roleNames);

        if (roles.size() != roleNames.size()) {
            throw new InvalidRoleException(
                    "One or more roles are invalid"
            );
        }

        return roles;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

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