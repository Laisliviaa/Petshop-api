package com.example.petshopapi.apikey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Filtro de autenticação via API Key.
 *
 * - GET e OPTIONS são sempre públicos (sem autenticação).
 * - POST em /api/v1/apikeys é público (geração de chave).
 * - Demais operações exigem X-API-Key válida e ativa.
 *
 * Roles e permissões:
 *  VISITANTE   → apenas GET
 *  FUNCIONARIO → GET + POST + PUT
 *  ADMIN       → GET + POST + PUT + DELETE + revogar chaves
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Set<String> WRITE_METHODS =
            Set.of(HttpMethod.POST.name(), HttpMethod.PUT.name(),
                   HttpMethod.PATCH.name(), HttpMethod.DELETE.name());

    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        String path   = request.getRequestURI();

        if (HttpMethod.GET.name().equals(method))     return true;
        if ("OPTIONS".equals(method))                  return true;
        if (path.equals("/api/v1/apikeys")
                && HttpMethod.POST.name().equals(method)) return true;

        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String method = request.getMethod().toUpperCase();
        String path   = request.getRequestURI();
        String rawKey = request.getHeader("X-API-Key");

        if (rawKey == null || rawKey.isBlank()) {
            reject(response, request, 401,
                    "Header X-API-Key ausente. Gere uma chave em POST /api/v1/apikeys "
                    + "ou use uma das chaves pré-carregadas.");
            return;
        }

        Optional<ApiKey> opt = apiKeyRepository.findByKeyValueAndActiveTrue(rawKey);
        if (opt.isEmpty()) {
            log.warn("X-API-Key inválida ou revogada — path={}", path);
            reject(response, request, 401,
                    "X-API-Key inválida ou revogada. "
                    + "Gere uma nova chave em POST /api/v1/apikeys.");
            return;
        }

        ApiKey key  = opt.get();
        ApiKey.Role role = key.getRole();

        // VISITANTE: apenas leitura (GET já foi liberado no shouldNotFilter)
        if (role == ApiKey.Role.VISITANTE) {
            reject(response, request, 403,
                    "Sua chave tem role VISITANTE (somente leitura). "
                    + "Use uma chave FUNCIONARIO ou ADMIN para operações de escrita.");
            return;
        }

        // FUNCIONARIO: não pode fazer DELETE
        if (role == ApiKey.Role.FUNCIONARIO && HttpMethod.DELETE.name().equals(method)) {
            reject(response, request, 403,
                    "Sua chave tem role FUNCIONARIO e não pode excluir registros. "
                    + "Apenas ADMIN pode realizar exclusões.");
            return;
        }

        // Autenticado e autorizado
        log.info("Auth OK — clientName={} role={} {} {}",
                key.getClientName(), role, method, path);
        response.setHeader("X-API-Key-Role", role.name());
        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse res, HttpServletRequest req,
                        int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        String erro = status == 401 ? "Unauthorized" : "Forbidden";
        res.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"erro\":\"%s\","
                + "\"mensagem\":\"%s\",\"caminho\":\"%s\",\"metodo\":\"%s\"}",
                Instant.now(), status, erro, message,
                req.getRequestURI(), req.getMethod()));
    }
}
