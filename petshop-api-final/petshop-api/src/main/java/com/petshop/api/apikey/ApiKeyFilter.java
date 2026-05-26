package com.petshop.api.apikey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação via cabeçalho {@code X-API-Key}.
 * <p>
 * Registrado e ordenado pelo {@link com.petshop.api.infrastructure.WebConfig}.
 * <p>
 * Caminhos públicos (não exigem chave): /swagger-ui, /v3/api-docs, /h2-console, /api/v1/auth
 */
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-Key";

    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.startsWith("/api/v1/auth");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader(HEADER);

        if (key == null || key.isBlank()) {
            reject(response, "Cabeçalho X-API-Key ausente. Gere sua chave em POST /api/v1/auth/keys");
            return;
        }

        boolean valid = apiKeyRepository.findByChaveAndAtivaTrue(key).isPresent();
        if (!valid) {
            reject(response, "Chave de API inválida ou inativa.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}"
        );
    }
}
