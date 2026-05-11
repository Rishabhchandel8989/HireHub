package com.hirehub.controller;

import com.hirehub.model.User;
import com.hirehub.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only management endpoints.
 * Base path: /admin
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /admin/users?page=0&size=20
     * Returns paginated list of all registered users.
     */
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    /**
     * PUT /admin/users/{userId}/toggle
     * Enable or disable a user account.
     */
    @PutMapping("/users/{userId}/toggle")
    public ResponseEntity<Void> toggleUser(@PathVariable Long userId) {
        adminService.toggleUserEnabled(userId);
        return ResponseEntity.noContent().build();
    }
}
