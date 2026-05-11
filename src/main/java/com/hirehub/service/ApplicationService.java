package com.hirehub.service;

import com.hirehub.dto.application.ApplicationRequest;
import com.hirehub.dto.application.ApplicationResponse;
import com.hirehub.dto.application.StatusUpdateRequest;
import com.hirehub.exception.DuplicateResourceException;
import com.hirehub.exception.ResourceNotFoundException;
import com.hirehub.exception.UnauthorizedException;
import com.hirehub.model.*;
import com.hirehub.repository.ApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for submitting, tracking, and managing job applications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    // ── Apply ─────────────────────────────────────────────────────────────────

    @Transactional
    public ApplicationResponse apply(ApplicationRequest request, Long candidateId) {
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId()));

        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot apply to a job that is not ACTIVE");
        }

        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidateId)) {
            throw new DuplicateResourceException(
                    "You have already applied to job: " + job.getTitle());
        }

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", candidateId));

        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.APPLIED)
                .build();

        application = applicationRepository.save(application);
        log.info("Candidate {} applied to job {}", candidateId, job.getId());
        return mapToResponse(application);
    }

    // ── Withdraw ─────────────────────────────────────────────────────────────

    @Transactional
    public void withdraw(Long applicationId, Long candidateId) {
        Application application = getById(applicationId);
        if (!application.getCandidate().getId().equals(candidateId)) {
            throw new UnauthorizedException("You can only withdraw your own applications");
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    // ── Status Update (Recruiter / Admin) ─────────────────────────────────────

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId,
                                            StatusUpdateRequest request,
                                            Long currentUserId,
                                            Role currentRole) {
        Application application = getById(applicationId);

        // Only the recruiter who posted the job (or admin) may update status
        if (currentRole != Role.ROLE_ADMIN
                && !application.getJob().getPostedBy().getId().equals(currentUserId)) {
            throw new UnauthorizedException(
                    "You are not authorised to update this application status");
        }

        application.setStatus(request.getStatus());
        return mapToResponse(applicationRepository.save(application));
    }

    // ── Reads ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(Long candidateId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        return applicationRepository.findByCandidateId(candidateId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsForJob(Long jobId,
                                                            Long currentUserId,
                                                            Role currentRole,
                                                            int page, int size) {
        // Validate job exists
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        // Only the posting recruiter or admin may see all applications
        if (currentRole != Role.ROLE_ADMIN
                && !job.getPostedBy().getId().equals(currentUserId)) {
            throw new UnauthorizedException(
                    "You are not authorised to view applications for this job");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        return applicationRepository.findByJobId(jobId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId, Long currentUserId, Role currentRole) {
        Application app = getById(applicationId);
        boolean isOwner = app.getCandidate().getId().equals(currentUserId);
        boolean isRecruiter = app.getJob().getPostedBy().getId().equals(currentUserId);
        boolean isAdmin = currentRole == Role.ROLE_ADMIN;

        if (!isOwner && !isRecruiter && !isAdmin) {
            throw new UnauthorizedException("Access denied to this application");
        }
        return mapToResponse(app);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Application getById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
    }

    private ApplicationResponse mapToResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .company(app.getJob().getCompany())
                .candidateId(app.getCandidate().getId())
                .candidateName(app.getCandidate().getFullName())
                .candidateEmail(app.getCandidate().getEmail())
                .status(app.getStatus())
                .coverLetter(app.getCoverLetter())
                .appliedAt(app.getAppliedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
