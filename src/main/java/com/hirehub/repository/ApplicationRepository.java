package com.hirehub.repository;

import com.hirehub.model.Application;
import com.hirehub.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /** All applications submitted by a specific candidate */
    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);

    /** All applications for a specific job */
    Page<Application> findByJobId(Long jobId, Pageable pageable);

    /** Check for duplicate application */
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    /** Count applications per job */
    long countByJobId(Long jobId);

    /** Applications by status for a job */
    List<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status);

    /** Find a specific candidate's application to a specific job */
    Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId);
}
