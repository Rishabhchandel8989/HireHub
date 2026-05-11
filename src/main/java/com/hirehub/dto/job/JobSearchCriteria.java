package com.hirehub.dto.job;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Search & filter criteria for job listings.
 * All fields are optional; null values are ignored in dynamic queries.
 */
@Data
public class JobSearchCriteria {

    /** Free-text search against title and description */
    private String keyword;

    private String location;

    /** Required skill (substring match against skillsRequired) */
    private String skill;

    private BigDecimal minSalary;
    private BigDecimal maxSalary;

    private Integer minExperience;
    private Integer maxExperience;
}
