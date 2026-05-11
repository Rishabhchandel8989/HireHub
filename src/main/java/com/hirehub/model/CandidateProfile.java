package com.hirehub.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Extended profile for CANDIDATE users.
 * Stores skills, experience, location, and resume URL.
 */
@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 100)
    private String phone;

    @Column(length = 200)
    private String location;

    /**
     * Comma-separated list of skills e.g. "Java,Spring Boot,MySQL"
     */
    @Column(columnDefinition = "TEXT")
    private String skills;

    /**
     * Total years of experience.
     */
    @Column
    private Integer experienceYears;

    @Column(length = 500)
    private String resumeUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;
}
