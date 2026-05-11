package com.hirehub.controller;

import com.hirehub.dto.job.JobRequest;
import com.hirehub.dto.job.JobResponse;
import com.hirehub.dto.job.JobSearchCriteria;
import com.hirehub.model.Role;
import com.hirehub.security.CustomUserDetails;
import com.hirehub.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Job management and search endpoints.
 * Base path: /jobs
 */
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // ── Public: Search & Browse ───────────────────────────────────────────────

    /**
     * GET /jobs?keyword=&location=&skill=&minSalary=&maxSalary=&minExperience=&maxExperience=
     *          &page=0&size=10&sortBy=createdAt
     */
    @GetMapping
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setLocation(location);
        criteria.setSkill(skill);
        criteria.setMinSalary(minSalary);
        criteria.setMaxSalary(maxSalary);
        criteria.setMinExperience(minExperience);
        criteria.setMaxExperience(maxExperience);

        return ResponseEntity.ok(jobService.searchJobs(criteria, page, size, sortBy));
    }

    /**
     * GET /jobs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    // ── Recruiter: My Posted Jobs ─────────────────────────────────────────────

    /**
     * GET /jobs/my – list all jobs posted by the currently authenticated recruiter
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    public ResponseEntity<List<JobResponse>> getMyJobs(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(user.getId()));
    }

    // ── Recruiter / Admin: Create, Update, Delete ─────────────────────────────

    /**
     * POST /jobs
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        JobResponse response = jobService.createJob(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /jobs/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        Role role = Role.valueOf(
                user.getAuthorities().iterator().next().getAuthority());
        return ResponseEntity.ok(jobService.updateJob(id, request, user.getId(), role));
    }

    /**
     * DELETE /jobs/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        Role role = Role.valueOf(
                user.getAuthorities().iterator().next().getAuthority());
        jobService.deleteJob(id, user.getId(), role);
        return ResponseEntity.noContent().build();
    }
}
