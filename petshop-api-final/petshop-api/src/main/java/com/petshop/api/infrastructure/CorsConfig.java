package com.petshop.api.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração global de CORS.
 * <p>
 * Permite que frontends (React, Angular, etc.) consumam a API
 * sem bloqueio de origem cruzada.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(
                        "Location",
                        "X-API-Key",
                        "X-Idempotency-Key",
                        "X-RateLimit-Limit",
                        "X-RateLimit-Remaining",
                        "Retry-After"
                )
                .allowCredentials(false)
                .maxAge(3600);
    }
}
