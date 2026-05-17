package payment.system.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import payment.system.app.dto.CreateRoleRequest;
import payment.system.app.dto.RoleResponseDto;
import payment.system.app.dto.UpdateRoleNameRequest;
import payment.system.app.dto.UpdateRoleRequest;
import payment.system.app.service.RoleService;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private static final Logger logger =
            LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    /**
     * Create Role
     */
    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping
    public ResponseEntity<RoleResponseDto> createRole(
            @Valid @RequestBody CreateRoleRequest dto) {

        logger.info("Create role request received");

        if (dto == null) {

            logger.error("CreateRoleRequest body is null");

            throw new IllegalArgumentException(
                    "Role request cannot be null");
        }

        logger.info(
                "Creating role with name : {}",
                dto.getName());

        RoleResponseDto response =
                roleService.createRole(dto);

        if (response == null) {

            logger.error(
                    "Role creation failed for role : {}",
                    dto.getName());

            throw new RuntimeException(
                    "Failed to create role");
        }

        logger.info(
                "Role created successfully with name : {}",
                dto.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * Update Role Name + Permissions
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{roleId}")
    public ResponseEntity<RoleResponseDto> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest dto) {

        logger.info(
                "Update role request received for roleId : {}",
                roleId);

        if (roleId == null || roleId <= 0) {

            logger.error(
                    "Invalid roleId received : {}",
                    roleId);

            throw new IllegalArgumentException(
                    "Valid roleId is required");
        }

        if (dto == null) {

            logger.error(
                    "UpdateRoleRequest body is null");

            throw new IllegalArgumentException(
                    "Update role request cannot be null");
        }

        logger.info(
                "Updating role id : {}",
                roleId);

        RoleResponseDto response =
                roleService.updateRole(
                        roleId,
                        dto);

        if (response == null) {

            logger.error(
                    "Role update failed for roleId : {}",
                    roleId);

            throw new RuntimeException(
                    "Failed to update role");
        }

        logger.info(
                "Role updated successfully for roleId : {}",
                roleId);

        return ResponseEntity.ok(response);
    }

    /**
     * Update Only Role Name
     */
    @PatchMapping("/{roleId}/name")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<RoleResponseDto> updateRoleName(
            @PathVariable Long roleId,
            @RequestBody UpdateRoleNameRequest dto) {

        logger.info(
                "Update role name request received for roleId : {}",
                roleId);

        if (roleId == null || roleId <= 0) {

            logger.error(
                    "Invalid roleId received : {}",
                    roleId);

            throw new IllegalArgumentException(
                    "Valid roleId is required");
        }

        if (dto == null) {

            logger.error(
                    "UpdateRoleNameRequest body is null");

            throw new IllegalArgumentException(
                    "Update role name request cannot be null");
        }

        logger.info(
                "Updating role name for roleId : {}",
                roleId);

        RoleResponseDto response =
                roleService.updateRoleName(
                        roleId,
                        dto);

        if (response == null) {

            logger.error(
                    "Role name update failed for roleId : {}",
                    roleId);

            throw new RuntimeException(
                    "Failed to update role name");
        }

        logger.info(
                "Role name updated successfully for roleId : {}",
                roleId);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete Role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<String> deleteRole(
            @PathVariable Long id) {

        logger.info(
                "Delete role request received for roleId : {}",
                id);

        if (id == null || id <= 0) {

            logger.error(
                    "Invalid role id received for delete : {}",
                    id);

            throw new IllegalArgumentException(
                    "Valid role id is required");
        }

        roleService.deleteRole(id);

        logger.info(
                "Role deleted successfully for roleId : {}",
                id);

        return ResponseEntity.ok(
                "Role deleted successfully");
    }
}