package com.very.relink.core.configuration.security;

import com.very.relink.core.infra.security.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class CorsConfiguration {

    private final CorsProperties properties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOriginPatterns(properties.origins());
        config.setAllowedMethods(properties.methods());
        config.setAllowedHeaders(properties.headers());
        config.setExposedHeaders(properties.exposedHeaders());
        config.setMaxAge(properties.maxAge());
        config.setAllowCredentials(properties.credentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(properties.paths(), config);
        return source;
    }
}
