package com.hirehub.dto.candidate;

import lombok.Builder;
import lombok.Data;

/**
 * Candidate profile response DTO.
 */
@Data
@Builder
public class CandidateProfileResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String skills;
    private Integer experienceYears;
    private String resumeUrl;
    private String bio;
}
