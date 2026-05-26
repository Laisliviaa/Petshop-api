package com.petshop.api.infrastructure;

import com.petshop.api.apikey.ApiKeyFilter;
import com.petshop.api.apikey.ApiKeyRepository;
import com.petshop.api.ratelimit.RateLimitConfig;
import com.petshop.api.ratelimit.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra e ordena os filtros da aplicação:
 * <ol>
 *   <li>RateLimitFilter  — verificado primeiro (order 1): 30 GET/min · 10 escrita/min</li>
 *   <li>ApiKeyFilter     — autenticação (order 2): X-API-Key obrigatório</li>
 * </ol>
 *
 * Os filtros NÃO são anotados com {@code @Component} para evitar registro duplo pelo Spring Boot.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final RateLimitConfig  rateLimitConfig;
    private final ApiKeyRepository apiKeyRepository;

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration() {
        FilterRegistrationBean<RateLimitFilter> bean =
                new FilterRegistrationBean<>(new RateLimitFilter(rateLimitConfig));
        bean.addUrlPatterns("/api/*");
        bean.setOrder(1);
        bean.setName("rateLimitFilter");
        return bean;
    }

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration() {
        FilterRegistrationBean<ApiKeyFilter> bean =
                new FilterRegistrationBean<>(new ApiKeyFilter(apiKeyRepository));
        bean.addUrlPatterns("/api/*");
        bean.setOrder(2);
        bean.setName("apiKeyFilter");
        return bean;
    }
}
