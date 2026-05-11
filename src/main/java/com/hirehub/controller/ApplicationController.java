package com.hirehub.controller;

import com.hirehub.dto.application.ApplicationRequest;
import com.hirehub.dto.application.ApplicationResponse;
import com.hirehub.dto.application.StatusUpdateRequest;
import com.hirehub.model.Role;
import com.hirehub.security.CustomUserDetails;
import com.hirehub.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Application tracking endpoints.
 * Base path: /applications
 */
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // ── Candidate Actions ─────────────────────────────────────────────────────

    /**
     * POST /applications – submit a job application
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE')")
    public ResponseEntity<ApplicationResponse> apply(
            @Valid @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        ApplicationResponse response = applicationService.apply(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /applications/my – paginated list of my applications
     */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE')")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                applicationService.getMyApplications(user.getId(), page, size));
    }

    /**
     * PUT /applications/{id}/withdraw – candidate withdraws their application
     */
    @PutMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE')")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        applicationService.withdraw(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // ── Recruiter / Admin Actions ─────────────────────────────────────────────

    /**
     * GET /applications/job/{jobId} – all applications for a specific job
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsForJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Role role = Role.valueOf(
                user.getAuthorities().iterator().next().getAuthority());
        return ResponseEntity.ok(
                applicationService.getApplicationsForJob(
                        jobId, user.getId(), role, page, size));
    }

    /**
     * PUT /applications/{id}/status – update application status (RECRUITER / ADMIN)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        Role role = Role.valueOf(
                user.getAuthorities().iterator().next().getAuthority());
        return ResponseEntity.ok(
                applicationService.updateStatus(id, request, user.getId(), role));
    }

    /**
     * GET /applications/{id} – get single application detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        Role role = Role.valueOf(
                user.getAuthorities().iterator().next().getAuthority());
        return ResponseEntity.ok(
                applicationService.getApplicationById(id, user.getId(), role));
    }
}
