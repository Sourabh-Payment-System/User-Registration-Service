package payment.system.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import payment.system.app.dto.PermissionDto;
import payment.system.app.dto.PermissionResponseDto;
import payment.system.app.service.PermissionService;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private static final Logger logger =
            LoggerFactory.getLogger(PermissionController.class);

    private final PermissionService permissionService;

    /**
     * Create Permission
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<PermissionResponseDto>
    createPermission(
            @Valid @RequestBody PermissionDto permissionDto) {

        logger.info(
                "Create permission request received");

        if (permissionDto == null) {

            logger.error(
                    "PermissionDto request body is null");

            throw new IllegalArgumentException(
                    "Permission request cannot be null");
        }

        logger.info(
                "Creating permission with name : {}",
                permissionDto.getName());

        PermissionResponseDto response =
                permissionService.createPermission(
                        permissionDto);

        if (response == null) {

            logger.error(
                    "Permission creation failed for permission : {}",
                    permissionDto.getName());

            throw new RuntimeException(
                    "Failed to create permission");
        }

        logger.info(
                "Permission created successfully with name : {}",
                permissionDto.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * Update Permission
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<PermissionResponseDto>
    updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionDto request) {

        logger.info(
                "Update permission request received for id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid permission id received : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid permission id is required");
        }

        if (request == null) {

            logger.error(
                    "Permission update request body is null");

            throw new IllegalArgumentException(
                    "Permission update request cannot be null");
        }

        logger.info(
                "Updating permission id : {} with name : {}",
                id,
                request.getName());

        PermissionResponseDto response =
                permissionService.updatePermission(
                        id,
                        request);

        if (response == null) {

            logger.error(
                    "Permission update failed for id : {}",
                    id);

            throw new RuntimeException(
                    "Failed to update permission");
        }

        logger.info(
                "Permission updated successfully for id : {}",
                id);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete Permission
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<String> deletePermission(
            @PathVariable Long id) {

        logger.info(
                "Delete permission request received for id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid permission id received for delete : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid permission id is required");
        }

        permissionService.deletePermission(id);

        logger.info(
                "Permission deleted successfully for id : {}",
                id);

        return ResponseEntity.ok(
                "Permission deleted successfully");
    }
}