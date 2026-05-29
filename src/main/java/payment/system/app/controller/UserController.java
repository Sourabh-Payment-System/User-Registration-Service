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

        logger.info(
                "Create user request received for email={}",
                request.getEmail());

        UserResponse response =
                userService.createUser(request);

        logger.info(
                "User created successfully with id={}",
                response.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Get All Users
     */
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        logger.info("Fetch all users request received");

        List<UserResponse> users =
                userService.getAllUsers();

        logger.info(
                "Fetched {} users successfully",
                users.size());

        return ResponseEntity.ok(users);
    }

    /**
     * Get User By Id
     */
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        logger.info(
                "Fetch user request received for userId={}",
                id);

        UserResponse response =
                userService.getUserById(id);

        logger.info(
                "User fetched successfully for userId={}",
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
                "Update user request received for userId={}",
                id);

        UserResponse response =
                userService.updateUser(id, request);

        logger.info(
                "User updated successfully for userId={}",
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
                "Delete user request received for userId={}",
                id);

        userService.deleteUser(id);

        logger.info(
                "User deleted successfully for userId={}",
                id);

        return ResponseEntity.ok(
                "User deleted successfully");
    }
}