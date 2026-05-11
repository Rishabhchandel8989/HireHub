package com.hirehub.service;

import com.hirehub.dto.candidate.CandidateProfileRequest;
import com.hirehub.dto.candidate.CandidateProfileResponse;
import com.hirehub.exception.ResourceNotFoundException;
import com.hirehub.model.CandidateProfile;
import com.hirehub.model.User;
import com.hirehub.repository.CandidateProfileRepository;
import com.hirehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Manages candidate profile creation and updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    public CandidateProfileResponse upsertProfile(Long userId, CandidateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        CandidateProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> CandidateProfile.builder().user(user).build());

        if (StringUtils.hasText(request.getPhone()))         profile.setPhone(request.getPhone());
        if (StringUtils.hasText(request.getLocation()))      profile.setLocation(request.getLocation());
        if (StringUtils.hasText(request.getSkills()))        profile.setSkills(request.getSkills());
        if (request.getExperienceYears() != null)            profile.setExperienceYears(request.getExperienceYears());
        if (StringUtils.hasText(request.getResumeUrl()))     profile.setResumeUrl(request.getResumeUrl());
        if (StringUtils.hasText(request.getBio()))           profile.setBio(request.getBio());

        profile = profileRepository.save(profile);
        return mapToResponse(user, profile);
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        CandidateProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CandidateProfile", "userId", userId));

        return mapToResponse(user, profile);
    }

    private CandidateProfileResponse mapToResponse(User user, CandidateProfile profile) {
        return CandidateProfileResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(profile.getPhone())
                .location(profile.getLocation())
                .skills(profile.getSkills())
                .experienceYears(profile.getExperienceYears())
                .resumeUrl(profile.getResumeUrl())
                .bio(profile.getBio())
                .build();
    }
}
