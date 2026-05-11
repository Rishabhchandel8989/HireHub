package com.hirehub.dto.application;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload when a CANDIDATE applies to a job.
 */
@Data
public class ApplicationRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    private String coverLetter;
}
