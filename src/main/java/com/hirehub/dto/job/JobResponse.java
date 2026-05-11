package com.hirehub.dto.job;

import com.hirehub.model.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a Job – avoids exposing entity directly.
 */
@Data
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String skillsRequired;
    private Integer minExperience;
    private Integer maxExperience;
    private JobStatus status;
    private Long postedById;
    private String postedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int totalApplications;
}
