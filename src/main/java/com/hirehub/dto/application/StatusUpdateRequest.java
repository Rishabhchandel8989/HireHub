package com.hirehub.dto.application;

import com.hirehub.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload to update application status (RECRUITER / ADMIN).
 */
@Data
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}
