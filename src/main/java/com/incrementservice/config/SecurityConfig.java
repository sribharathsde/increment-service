package com.incrementservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class that defines security settings for the application. This class enables web security and
 * configures HTTP security, including CSRF protection and request authorization.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     *
     * <p>This method disables CSRF protection and permits all requests to actuator endpoints
     * while requiring authentication for all other requests.</p>
     *
     * @param http the {@link HttpSecurity} to modify
     * @return the configured {@link SecurityFilterChain}
     * @exception Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(
                        authz -> authz.requestMatchers("/actuator/**").permitAll().requestMatchers("/api/**").authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
