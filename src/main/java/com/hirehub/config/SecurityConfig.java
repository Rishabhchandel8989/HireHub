package com.hirehub.config;

import com.hirehub.security.JwtAuthenticationFilter;
import com.hirehub.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 * - Stateless (JWT-based) sessions
 * - Role-based endpoint access
 * - Method-level security via @PreAuthorize
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Public endpoints
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/jobs/**").permitAll()

                    // Job management – RECRUITER & ADMIN
                    .requestMatchers(HttpMethod.POST, "/jobs/**")
                            .hasAnyAuthority("ROLE_RECRUITER", "ROLE_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/jobs/**")
                            .hasAnyAuthority("ROLE_RECRUITER", "ROLE_ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/jobs/**")
                            .hasAnyAuthority("ROLE_RECRUITER", "ROLE_ADMIN")

                    // Applications
                    .requestMatchers(HttpMethod.POST, "/applications/**")
                            .hasAuthority("ROLE_CANDIDATE")
                    .requestMatchers("/applications/**")
                            .hasAnyAuthority("ROLE_CANDIDATE", "ROLE_RECRUITER", "ROLE_ADMIN")

                    // Admin-only routes
                    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                    // Candidate profile
                    .requestMatchers("/candidates/**")
                            .hasAnyAuthority("ROLE_CANDIDATE", "ROLE_RECRUITER", "ROLE_ADMIN")

                    // Everything else requires authentication
                    .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
