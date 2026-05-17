package payment.system.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.PermissionDto;
import payment.system.app.dto.PermissionResponseDto;
import payment.system.app.entity.Permission;
import payment.system.app.repository.PermissionRepository;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    PermissionService.class);

    private final PermissionRepository permissionRepository;

    /**
     * Create Permission
     */
    public PermissionResponseDto createPermission(
            PermissionDto permissionDto) {

        logger.info("Create permission service started");

        if (permissionDto == null) {

            logger.error(
                    "PermissionDto is null");

            throw new IllegalArgumentException(
                    "Permission request cannot be null");
        }

        if (permissionDto.getName() == null
                || permissionDto.getName().isBlank()) {

            logger.error(
                    "Permission name is null or empty");

            throw new IllegalArgumentException(
                    "Permission name cannot be null or empty");
        }

        logger.info(
                "Checking duplicate permission for name : {}",
                permissionDto.getName());

        /**
         * Check duplicate permission
         */
        if (permissionRepository.findByName(
                permissionDto.getName()).isPresent()) {

            logger.error(
                    "Permission already exists with name : {}",
                    permissionDto.getName());

            throw new IllegalArgumentException(
                    "Permission already exists: "
                            + permissionDto.getName()
            );
        }

        /**
         * Create entity
         */
        Permission permission = new Permission();

        permission.setName(permissionDto.getName());

        logger.info(
                "Saving permission with name : {}",
                permissionDto.getName());

        /**
         * Save permission
         */
        Permission savedPermission =
                permissionRepository.save(permission);

        if (savedPermission == null) {

            logger.error(
                    "Permission save operation failed");

            throw new RuntimeException(
                    "Failed to save permission");
        }

        logger.info(
                "Permission created successfully with id : {}",
                savedPermission.getId());

        /**
         * Entity -> DTO
         */
        return PermissionResponseDto.builder()
                .id(savedPermission.getId())
                .name(savedPermission.getName())
                .build();
    }

    /**
     * Update Permission
     */
    public PermissionResponseDto updatePermission(
            Long id,
            PermissionDto request) {

        logger.info(
                "Update permission service started for id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid permission id : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid permission id is required");
        }

        if (request == null) {

            logger.error(
                    "Permission update request is null");

            throw new IllegalArgumentException(
                    "Permission update request cannot be null");
        }

        if (request.getName() == null
                || request.getName().isBlank()) {

            logger.error(
                    "Permission name is null or empty");

            throw new IllegalArgumentException(
                    "Permission name cannot be null or empty");
        }

        logger.info(
                "Fetching permission with id : {}",
                id);

        Permission permission =
                permissionRepository.findById(id)
                        .orElseThrow(() -> {

                            logger.error(
                                    "Permission not found with id : {}",
                                    id);

                            return new IllegalArgumentException(
                                    "Permission not found with id: "
                                            + id
                            );
                        });

        /**
         * Check duplicate name
         */
        logger.info(
                "Checking duplicate permission name : {}",
                request.getName());

        permissionRepository.findByName(request.getName())
                .filter(existing ->
                        !existing.getId().equals(id)
                )
                .ifPresent(existing -> {

                    logger.error(
                            "Permission name already exists : {}",
                            request.getName());

                    throw new IllegalArgumentException(
                            "Permission name already exists: "
                                    + request.getName()
                    );
                });

        permission.setName(request.getName());

        logger.info(
                "Updating permission with id : {}",
                id);

        Permission updatedPermission =
                permissionRepository.save(permission);

        if (updatedPermission == null) {

            logger.error(
                    "Permission update failed for id : {}",
                    id);

            throw new RuntimeException(
                    "Failed to update permission");
        }

        logger.info(
                "Permission updated successfully for id : {}",
                id);

        return PermissionResponseDto.builder()
                .id(updatedPermission.getId())
                .name(updatedPermission.getName())
                .build();
    }

    /**
     * Delete Permission
     */
    public void deletePermission(Long id) {

        logger.info(
                "Delete permission service started for id : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid permission id : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid permission id is required");
        }

        Permission permission =
                permissionRepository.findById(id)
                        .orElseThrow(() -> {

                            logger.error(
                                    "Permission not found with id : {}",
                                    id);

                            return new IllegalArgumentException(
                                    "Permission not found with id: "
                                            + id);
                        });

        logger.info(
                "Deleting permission with id : {}",
                id);

        permissionRepository.delete(permission);

        logger.info(
                "Permission deleted successfully for id : {}",
                id);
    }
}