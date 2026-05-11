package com.hirehub.controller;

import com.hirehub.dto.candidate.CandidateProfileRequest;
import com.hirehub.dto.candidate.CandidateProfileResponse;
import com.hirehub.security.CustomUserDetails;
import com.hirehub.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Candidate profile management endpoints.
 * Base path: /candidates
 */
@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    /**
     * GET /candidates/me – get my own profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE')")
    public ResponseEntity<CandidateProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(candidateService.getProfile(user.getId()));
    }

    /**
     * PUT /candidates/me – create or update my profile
     */
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE')")
    public ResponseEntity<CandidateProfileResponse> updateMyProfile(
            @RequestBody CandidateProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(candidateService.upsertProfile(user.getId(), request));
    }

    /**
     * GET /candidates/{userId} – view another candidate's profile (RECRUITER / ADMIN)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    public ResponseEntity<CandidateProfileResponse> getCandidateProfile(
            @PathVariable Long userId) {
        return ResponseEntity.ok(candidateService.getProfile(userId));
    }
}
