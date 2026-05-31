package com.example.petshopapi.versioning;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Versionamento de API via header X-API-Version (Media Type / Header Versioning).
 *
 * Esta é a abordagem correta para versionamento RESTful, pois:
 *  - Mantém URLs limpas e estáveis (/api/v1/clientes para sempre)
 *  - O cliente negocia a versão via header, sem mudar o endpoint
 *  - Versão ausente assume "1" (backward compatible)
 *  - Versão inválida retorna 400 com mensagem clara
 *
 * Versões suportadas: 1 (padrão) e 2
 *  - v1: resposta padrão com HATEOAS
 *  - v2: resposta enriquecida com metadados adicionais (totalElements, totalPages, etc.)
 *
 * Aplicado a todas as rotas /api/** via WebMvcConfig.
 */
@Slf4j
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final Set<String> SUPPORTED = Set.of("1", "2");

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String raw = request.getHeader("X-API-Version");

        if (raw == null || raw.isBlank()) {
            // Versão ausente: assume v1 (backward compatibility)
            response.setHeader("X-API-Version", "1");
            request.setAttribute("apiVersion", "1");
            return true;
        }

        String version = raw.trim().toLowerCase().replace("v", "");

        if (!SUPPORTED.contains(version)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                    "{\"timestamp\":\"%s\",\"status\":400,\"erro\":\"Bad Request\","
                    + "\"mensagem\":\"Versão de API inválida: '%s'. "
                    + "Versões suportadas: 1 (padrão) ou 2. "
                    + "Use o header X-API-Version: 1 ou X-API-Version: 2.\","
                    + "\"caminho\":\"%s\"}",
                    java.time.Instant.now(), raw, request.getRequestURI()));
            return false;
        }

        log.debug("X-API-Version={} path={}", version, request.getRequestURI());
        response.setHeader("X-API-Version", version);
        request.setAttribute("apiVersion", version);
        return true;
    }
}
