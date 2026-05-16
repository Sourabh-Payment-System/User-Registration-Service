package payment.system.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import payment.system.app.dto.CreateRoleRequest;
import payment.system.app.dto.RoleDto;
import payment.system.app.dto.RoleResponseDto;
import payment.system.app.dto.UpdateRoleNameRequest;
import payment.system.app.dto.UpdateRoleRequest;
import payment.system.app.service.RoleService;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Create Role
     */
    @PostMapping
    public ResponseEntity<RoleResponseDto> createRole(
            @Valid @RequestBody CreateRoleRequest dto) {

        return ResponseEntity.ok(
                roleService.createRole(dto)
        );
    }

    /**
     * Update Role Name + Permissions
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<RoleResponseDto> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest dto) {

        return ResponseEntity.ok(
                roleService.updateRole(roleId, dto)
        );
    }

    /**
     * Update Only Role Name
     */
    @PatchMapping("/{roleId}/name")
    public ResponseEntity<RoleResponseDto> updateRoleName(
            @PathVariable Long roleId,
            @RequestBody UpdateRoleNameRequest dto) {

        return ResponseEntity.ok(
                roleService.updateRoleName(roleId, dto)
        );
    }
    
    /**
     * Delete Role
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(
            @PathVariable Long id) {

    	roleService.deleteRole(id);

        return ResponseEntity.ok("Role deleted successfully");
    }
}