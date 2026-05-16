package payment.system.app.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.CreateRoleRequest;
import payment.system.app.dto.RoleDto;
import payment.system.app.dto.RoleResponseDto;
import payment.system.app.dto.UpdateRoleNameRequest;
import payment.system.app.dto.UpdateRoleRequest;
import payment.system.app.entity.Permission;
import payment.system.app.entity.Role;
import payment.system.app.entity.User;
import payment.system.app.repository.PermissionRepository;
import payment.system.app.repository.RoleRepository;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    /**
     * Create Role
     */
    public RoleResponseDto createRole(CreateRoleRequest dto) {

        /**
         * Check duplicate role
         */
        if (roleRepository.findByName(dto.getName()).isPresent()) {

            throw new RuntimeException(
                    "Role already exists: " + dto.getName()
            );
        }

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
        if (permissions.size() != dto.getPermissions().size()) {

            throw new RuntimeException(
                    "One or more permissions are not present in system"
            );
        }

        /**
         * Create role
         */
        Role role = new Role();

        role.setName(dto.getName());

        role.setPermissions(permissions);

        /**
         * Save role
         */
        Role savedRole = roleRepository.save(role);

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

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RuntimeException("Role not found")
                );

        /**
         * Duplicate role validation
         */
        roleRepository.findByName(dto.getName())
                .filter(existing ->
                        !existing.getId().equals(roleId)
                )
                .ifPresent(r -> {
                    throw new RuntimeException(
                            "Role name already exists: "
                                    + dto.getName()
                    );
                });

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
        if (permissions.size() != dto.getPermissions().size()) {

            throw new RuntimeException(
                    "One or more permissions are not present in system"
            );
        }

        role.setName(dto.getName());

        role.setPermissions(permissions);

        Role updatedRole = roleRepository.save(role);

        return mapToResponse(updatedRole);
    }

    /**
     * Update Only Role Name
     */
    public RoleResponseDto updateRoleName(
            Long roleId,
            UpdateRoleNameRequest dto) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RuntimeException("Role not found")
                );

        /**
         * Duplicate role validation
         */
        roleRepository.findByName(dto.getName())
                .filter(existing ->
                        !existing.getId().equals(roleId)
                )
                .ifPresent(existing -> {
                    throw new RuntimeException(
                            "Role name already exists: "
                                    + dto.getName()
                    );
                });

        role.setName(dto.getName());

        Role updatedRole = roleRepository.save(role);

        return mapToResponse(updatedRole);
    }

    /**
     * Common Mapper Method
     */
    private RoleResponseDto mapToResponse(Role role) {

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

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        roleRepository.delete(role);
    }
}