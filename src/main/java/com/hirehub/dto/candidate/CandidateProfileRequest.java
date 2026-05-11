package com.hirehub.dto.candidate;

import lombok.Builder;
import lombok.Data;

/**
 * Candidate profile update DTO.
 */
@Data
@Builder
public class CandidateProfileRequest {
    private String phone;
    private String location;
    /** Comma-separated skills */
    private String skills;
    private Integer experienceYears;
    private String resumeUrl;
    private String bio;
}
