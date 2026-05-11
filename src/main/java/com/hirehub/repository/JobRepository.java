package com.hirehub.repository;

import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JpaSpecificationExecutor enables dynamic Criteria API queries for job search/filtering.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    List<Job> findByPostedByIdOrderByCreatedAtDesc(Long postedById);
    List<Job> findByStatusOrderByCreatedAtDesc(JobStatus status);
    long countByPostedById(Long postedById);
}
