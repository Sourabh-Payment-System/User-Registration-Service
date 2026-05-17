package payment.system.app.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import payment.system.app.dto.CreateUserRequest;
import payment.system.app.dto.UserResponse;
import payment.system.app.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger =
            LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * Create User
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        logger.info("Create user request received");

        if (request == null) {

            logger.error("CreateUserRequest body is null");

            throw new IllegalArgumentException(
                    "User request cannot be null");
        }

        logger.info(
                "Creating user with email : {}",
                request.getEmail());

        UserResponse response =
                userService.createUser(request);

        if (response == null) {

            logger.error(
                    "User creation failed for email : {}",
                    request.getEmail());

            throw new RuntimeException(
                    "Failed to create user");
        }

        logger.info(
                "User created successfully with email : {}",
                request.getEmail());

        return ResponseEntity.ok(response);
    }

    /**
     * Get All Users
     */
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        logger.info("Get all users request received");

        List<UserResponse> users =
                userService.getAllUsers();

        logger.info(
                "Successfully fetched {} users",
                users.size());

        return ResponseEntity.ok(users);
    }

    /**
     * Get User By Id
     */
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        logger.info(
                "Get user by id request received for id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid user id received : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid user id is required");
        }

        UserResponse response =
                userService.getUserById(id);

        if (response == null) {

            logger.error(
                    "User not found for id : {}",
                    id);

            throw new RuntimeException(
                    "User not found");
        }

        logger.info(
                "User fetched successfully for id : {}",
                id);

        return ResponseEntity.ok(response);
    }

    /**
     * Update User
     */
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {

        logger.info(
                "Update user request received for id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid user id received for update : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid user id is required");
        }

        if (request == null) {

            logger.error(
                    "Update user request body is null");

            throw new IllegalArgumentException(
                    "Update user request cannot be null");
        }

        logger.info(
                "Updating user with id : {} and email : {}",
                id,
                request.getEmail());

        UserResponse response =
                userService.updateUser(id, request);

        if (response == null) {

            logger.error(
                    "User update failed for id : {}",
                    id);

            throw new RuntimeException(
                    "Failed to update user");
        }

        logger.info(
                "User updated successfully for id : {}",
                id);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete User
     */
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        logger.info(
                "Delete user request received for id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid user id received for delete : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid user id is required");
        }

        userService.deleteUser(id);

        logger.info(
                "User deleted successfully for id : {}",
                id);

        return ResponseEntity.ok(
                "User deleted successfully");
    }
}