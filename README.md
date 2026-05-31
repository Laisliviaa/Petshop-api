package com.example.petshopapi.versioning;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra o ApiVersionInterceptor em todas as rotas /api/**.
 *
 * O versionamento é feito exclusivamente via header X-API-Version (header versioning),
 * que é a abordagem correta para APIs REST:
 *
 *  ✅ Header versioning → URL estável, versão negociada pelo cliente
 *  ❌ URL versioning (/v1/, /v2/) → mistura versão com endereço do recurso
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiVersionInterceptor apiVersionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiVersionInterceptor)
                .addPathPatterns("/api/**");
    }
}
