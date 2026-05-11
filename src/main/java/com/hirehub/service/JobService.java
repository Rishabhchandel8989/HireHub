package com.hirehub.service;

import com.hirehub.dto.job.*;
import com.hirehub.exception.ResourceNotFoundException;
import com.hirehub.exception.UnauthorizedException;
import com.hirehub.model.*;
import com.hirehub.repository.ApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for job posting, retrieval, search, and management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    // ── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public JobResponse createJob(JobRequest request, Long recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recruiterId));

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .company(request.getCompany())
                .location(request.getLocation())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .skillsRequired(request.getSkillsRequired())
                .minExperience(request.getMinExperience())
                .maxExperience(request.getMaxExperience())
                .status(request.getStatus() != null ? request.getStatus() : JobStatus.ACTIVE)
                .postedBy(recruiter)
                .build();

        job = jobRepository.save(job);
        log.info("Job created: '{}' by {}", job.getTitle(), recruiter.getEmail());
        return mapToResponse(job);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, Long currentUserId, Role currentRole) {
        Job job = getJobEntityById(jobId);
        enforceOwnershipOrAdmin(job, currentUserId, currentRole);

        if (StringUtils.hasText(request.getTitle()))       job.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getDescription())) job.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getCompany()))     job.setCompany(request.getCompany());
        if (StringUtils.hasText(request.getLocation()))    job.setLocation(request.getLocation());
        if (request.getSalaryMin() != null)                job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null)                job.setSalaryMax(request.getSalaryMax());
        if (StringUtils.hasText(request.getSkillsRequired())) job.setSkillsRequired(request.getSkillsRequired());
        if (request.getMinExperience() != null)            job.setMinExperience(request.getMinExperience());
        if (request.getMaxExperience() != null)            job.setMaxExperience(request.getMaxExperience());
        if (request.getStatus() != null)                   job.setStatus(request.getStatus());

        return mapToResponse(jobRepository.save(job));
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Transactional
    public void deleteJob(Long jobId, Long currentUserId, Role currentRole) {
        Job job = getJobEntityById(jobId);
        enforceOwnershipOrAdmin(job, currentUserId, currentRole);
        jobRepository.delete(job);
        log.info("Job {} deleted by userId {}", jobId, currentUserId);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        return mapToResponse(getJobEntityById(jobId));
    }

    /**
     * Dynamic paginated search using JPA Specifications (Criteria API).
     * All filter fields are optional.
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(JobSearchCriteria criteria,
                                         int page, int size, String sortBy) {
        Specification<Job> spec = buildSpecification(criteria);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return jobRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getJobsByRecruiter(Long recruiterId) {
        return jobRepository.findByPostedByIdOrderByCreatedAtDesc(recruiterId)
                .stream().map(this::mapToResponse).toList();
    }

    // ── Specification (Dynamic Query) ─────────────────────────────────────────

    private Specification<Job> buildSpecification(JobSearchCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only show ACTIVE jobs to public search
            predicates.add(cb.equal(root.get("status"), JobStatus.ACTIVE));

            if (StringUtils.hasText(c.getKeyword())) {
                String like = "%" + c.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (StringUtils.hasText(c.getLocation())) {
                predicates.add(cb.like(
                        cb.lower(root.get("location")),
                        "%" + c.getLocation().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(c.getSkill())) {
                predicates.add(cb.like(
                        cb.lower(root.get("skillsRequired")),
                        "%" + c.getSkill().toLowerCase() + "%"));
            }
            if (c.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMax"), c.getMinSalary()));
            }
            if (c.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMin"), c.getMaxSalary()));
            }
            if (c.getMinExperience() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("minExperience"), c.getMinExperience()));
            }
            if (c.getMaxExperience() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxExperience"), c.getMaxExperience()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Job getJobEntityById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
    }

    private void enforceOwnershipOrAdmin(Job job, Long currentUserId, Role currentRole) {
        if (currentRole != Role.ROLE_ADMIN
                && !job.getPostedBy().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to modify this job");
        }
    }

    private JobResponse mapToResponse(Job job) {
        long applicationCount = applicationRepository.countByJobId(job.getId());
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .company(job.getCompany())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .skillsRequired(job.getSkillsRequired())
                .minExperience(job.getMinExperience())
                .maxExperience(job.getMaxExperience())
                .status(job.getStatus())
                .postedById(job.getPostedBy().getId())
                .postedByName(job.getPostedBy().getFullName())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .totalApplications((int) applicationCount)
                .build();
    }
}
