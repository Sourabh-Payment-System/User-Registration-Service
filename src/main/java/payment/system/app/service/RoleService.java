package payment.system.app.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.CreateRoleRequest;
import payment.system.app.dto.RoleResponseDto;
import payment.system.app.dto.UpdateRoleNameRequest;
import payment.system.app.dto.UpdateRoleRequest;
import payment.system.app.entity.Permission;
import payment.system.app.entity.Role;
import payment.system.app.repository.PermissionRepository;
import payment.system.app.repository.RoleRepository;

@Service
@RequiredArgsConstructor
public class RoleService {

    private static final Logger logger =
            LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    /**
     * Create Role
     */
    public RoleResponseDto createRole(
            CreateRoleRequest dto) {

        logger.info("Create role service started");

        if (dto == null) {

            logger.error(
                    "CreateRoleRequest is null");

            throw new IllegalArgumentException(
                    "Role request cannot be null");
        }

        if (dto.getName() == null
                || dto.getName().isBlank()) {

            logger.error(
                    "Role name is null or empty");

            throw new IllegalArgumentException(
                    "Role name cannot be null or empty");
        }

        if (dto.getPermissions() == null
                || dto.getPermissions().isEmpty()) {

            logger.error(
                    "Permissions are null or empty");

            throw new IllegalArgumentException(
                    "At least one permission is required");
        }

        logger.info(
                "Checking duplicate role with name : {}",
                dto.getName());

        /**
         * Check duplicate role
         */
        if (roleRepository.findByName(
                dto.getName()).isPresent()) {

            logger.error(
                    "Role already exists with name : {}",
                    dto.getName());

            throw new IllegalArgumentException(
                    "Role already exists: "
                            + dto.getName()
            );
        }

        logger.info(
                "Fetching permissions for role : {}",
                dto.getName());

        /**
         * Fetch permissions
         */
        Set<Permission> permissions =
                permissionRepository.findByNameIn(
                        dto.getPermissions()
                );

        /**
         * Validate permissions
         */
        if (permissions.size()
                != dto.getPermissions().size()) {

            logger.error(
                    "One or more permissions are invalid for role : {}",
                    dto.getName());

            throw new IllegalArgumentException(
                    "One or more permissions are not present in system"
            );
        }

        /**
         * Create role
         */
        Role role = new Role();

        role.setName(dto.getName());

        role.setPermissions(permissions);

        logger.info(
                "Saving role with name : {}",
                dto.getName());

        /**
         * Save role
         */
        Role savedRole =
                roleRepository.save(role);

        if (savedRole == null) {

            logger.error(
                    "Failed to save role : {}",
                    dto.getName());

            throw new RuntimeException(
                    "Failed to save role");
        }

        logger.info(
                "Role created successfully with id : {}",
                savedRole.getId());

        /**
         * Entity -> DTO
         */
        return mapToResponse(savedRole);
    }

    /**
     * Update Role Name + Permissions
     */
    public RoleResponseDto updateRole(
            Long roleId,
            UpdateRoleRequest dto) {

        logger.info(
                "Update role service started for roleId : {}",
                roleId);

        if (roleId == null || roleId <= 0) {

            logger.error(
                    "Invalid roleId : {}",
                    roleId);

            throw new IllegalArgumentException(
                    "Valid roleId is required");
        }

        if (dto == null) {

            logger.error(
                    "UpdateRoleRequest is null");

            throw new IllegalArgumentException(
                    "Update role request cannot be null");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {

                    logger.error(
                            "Role not found with id : {}",
                            roleId);

                    return new IllegalArgumentException(
                            "Role not found with id : "
                                    + roleId);
                });

        /**
         * Duplicate role validation
         */
        logger.info(
                "Checking duplicate role name : {}",
                dto.getName());

        roleRepository.findByName(dto.getName())
                .filter(existing ->
                        !existing.getId().equals(roleId)
                )
                .ifPresent(r -> {

                    logger.error(
                            "Role name already exists : {}",
                            dto.getName());

                    throw new IllegalArgumentException(
                            "Role name already exists: "
                                    + dto.getName()
                    );
                });

        logger.info(
                "Fetching permissions for roleId : {}",
                roleId);

        /**
         * Fetch permissions
         */
        Set<Permission> permissions =
                permissionRepository.findByNameIn(
                        dto.getPermissions()
                );

        /**
         * Validate permissions
         */
        if (permissions.size()
                != dto.getPermissions().size()) {

            logger.error(
                    "One or more permissions are invalid");

            throw new IllegalArgumentException(
                    "One or more permissions are not present in system"
            );
        }

        role.setName(dto.getName());

        role.setPermissions(permissions);

        logger.info(
                "Updating role with id : {}",
                roleId);

        Role updatedRole =
                roleRepository.save(role);

        if (updatedRole == null) {

            logger.error(
                    "Failed to update role with id : {}",
                    roleId);

            throw new RuntimeException(
                    "Failed to update role");
        }

        logger.info(
                "Role updated successfully for roleId : {}",
                roleId);

        return mapToResponse(updatedRole);
    }

    /**
     * Update Only Role Name
     */
    public RoleResponseDto updateRoleName(
            Long roleId,
            UpdateRoleNameRequest dto) {

        logger.info(
                "Update role name service started for roleId : {}",
                roleId);

        if (roleId == null || roleId <= 0) {

            logger.error(
                    "Invalid roleId : {}",
                    roleId);

            throw new IllegalArgumentException(
                    "Valid roleId is required");
        }

        if (dto == null
                || dto.getName() == null
                || dto.getName().isBlank()) {

            logger.error(
                    "Role name request is invalid");

            throw new IllegalArgumentException(
                    "Role name cannot be null or empty");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {

                    logger.error(
                            "Role not found with id : {}",
                            roleId);

                    return new IllegalArgumentException(
                            "Role not found with id : "
                                    + roleId);
                });

        /**
         * Duplicate role validation
         */
        logger.info(
                "Checking duplicate role name : {}",
                dto.getName());

        roleRepository.findByName(dto.getName())
                .filter(existing ->
                        !existing.getId().equals(roleId)
                )
                .ifPresent(existing -> {

                    logger.error(
                            "Role name already exists : {}",
                            dto.getName());

                    throw new IllegalArgumentException(
                            "Role name already exists: "
                                    + dto.getName()
                    );
                });

        role.setName(dto.getName());

        logger.info(
                "Updating role name for roleId : {}",
                roleId);

        Role updatedRole =
                roleRepository.save(role);

        if (updatedRole == null) {

            logger.error(
                    "Failed to update role name for roleId : {}",
                    roleId);

            throw new RuntimeException(
                    "Failed to update role name");
        }

        logger.info(
                "Role name updated successfully for roleId : {}",
                roleId);

        return mapToResponse(updatedRole);
    }

    /**
     * Common Mapper Method
     */
    private RoleResponseDto mapToResponse(
            Role role) {

        logger.info(
                "Mapping role entity to response DTO for role : {}",
                role.getName());

        return RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(
                        role.getPermissions()
                                .stream()
                                .map(Permission::getName)
                                .collect(Collectors.toSet())
                )
                .build();
    }

    /**
     * Delete Role
     */
    public void deleteRole(Long id) {

        logger.info(
                "Delete role service started for roleId : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid role id : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid role id is required");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {

                    logger.error(
                            "Role not found with id : {}",
                            id);

                    return new IllegalArgumentException(
                            "Role not found with id : "
                                    + id);
                });

        logger.info(
                "Deleting role with id : {}",
                id);

        roleRepository.delete(role);

        logger.info(
                "Role deleted successfully for roleId : {}",
                id);
    }
}