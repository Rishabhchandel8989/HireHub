package com.hirehub.dto.job;

import com.hirehub.model.JobStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a job posting.
 */
@Data
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Company name is required")
    @Size(max = 150)
    private String company;

    @NotBlank(message = "Location is required")
    @Size(max = 150)
    private String location;

    @DecimalMin(value = "0.0", message = "Minimum salary must be non-negative")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", message = "Maximum salary must be non-negative")
    private BigDecimal salaryMax;

    /**
     * Comma-separated skills e.g. "Java,Spring Boot,MySQL"
     */
    private String skillsRequired;

    @Min(value = 0, message = "Minimum experience cannot be negative")
    private Integer minExperience;

    @Min(value = 0, message = "Maximum experience cannot be negative")
    private Integer maxExperience;

    private JobStatus status;
}
