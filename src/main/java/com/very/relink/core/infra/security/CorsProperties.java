package com.very.relink.core.infra.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cors.allowed")
public record CorsProperties(
        String paths,
        List<String> origins,
        List<String> methods,
        List<String> headers,
        List<String> exposedHeaders,
        Long maxAge,
        Boolean credentials
) {
}
