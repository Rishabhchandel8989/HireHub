package com.hirehub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so @CreatedDate / @LastModifiedDate
 * fields are automatically populated.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
