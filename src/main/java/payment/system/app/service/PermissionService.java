package payment.system.app.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import payment.system.app.dto.PermissionDto;
import payment.system.app.dto.PermissionResponseDto;
import payment.system.app.entity.Permission;
import payment.system.app.entity.Role;
import payment.system.app.repository.PermissionRepository;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    /**
     * Create Permission
     */
    public PermissionResponseDto createPermission(
            PermissionDto permissionDto) {

        /**
         * Check duplicate permission
         */
        if (permissionRepository.findByName(
                permissionDto.getName()).isPresent()) {

            throw new RuntimeException(
                    "Permission already exists: "
                            + permissionDto.getName()
            );
        }

        /**
         * Create entity
         */
        Permission permission = new Permission();

        permission.setName(permissionDto.getName());

        /**
         * Save permission
         */
        Permission savedPermission =
                permissionRepository.save(permission);

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

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Permission not found with id: " + id
                        )
                );

        /**
         * Check duplicate name
         */
        permissionRepository.findByName(request.getName())
                .filter(existing ->
                        !existing.getId().equals(id)
                )
                .ifPresent(existing -> {
                    throw new RuntimeException(
                            "Permission name already exists: "
                                    + request.getName()
                    );
                });

        permission.setName(request.getName());

        Permission updatedPermission =
                permissionRepository.save(permission);

        return PermissionResponseDto.builder()
                .id(updatedPermission.getId())
                .name(updatedPermission.getName())
                .build();
    }
    
    /**
     * Delete Permission
     */
    public void deletePermission(Long id) {

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        permissionRepository.delete(permission);
    }
}