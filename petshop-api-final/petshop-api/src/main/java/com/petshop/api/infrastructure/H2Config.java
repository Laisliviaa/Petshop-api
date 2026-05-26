package com.petshop.api.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para ambiente de desenvolvimento.
 * <p>
 * Desabilita CSRF, libera o H2 Console (exige frames) e permite
 * todas as requisições — a autenticação real é feita pelo {@code ApiKeyFilter}.
 * <p>
 * Em produção, substitua o datasource H2 por PostgreSQL/MySQL
 * e remova a liberação do h2-console.
 */
@Configuration
public class H2Config {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
