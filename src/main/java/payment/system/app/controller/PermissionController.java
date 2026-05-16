package payment.system.app.controller;

import org.springframework.http.ResponseEntity;
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

    private final PermissionService permissionService;

    /**
     * Create Permission
     */
    @PostMapping
    public ResponseEntity<PermissionResponseDto> createPermission(
            @Valid @RequestBody PermissionDto permissionDto) {

        return ResponseEntity.ok(
                permissionService.createPermission(permissionDto)
        );
    }

    /**
     * Update Permission
     */
    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponseDto> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionDto request) {

        return ResponseEntity.ok(
                permissionService.updatePermission(id, request)
        );
    }
    /**
     * Delete Permission
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(
            @PathVariable Long id) {

    	permissionService.deletePermission(id);

        return ResponseEntity.ok("Role deleted successfully");
    }
}