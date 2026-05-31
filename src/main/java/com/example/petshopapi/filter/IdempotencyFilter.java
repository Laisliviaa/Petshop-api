package com.example.petshopapi.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de idempotência via header X-Idempotency-Key.
 *
 * Aplica-se apenas a POST, PUT e PATCH.
 *
 * Regras:
 *  - Chave nova                           → processa normalmente e armazena resultado
 *  - Mesma chave + mesmo payload (hash)   → retorna resposta original sem reprocessar (200/201)
 *  - Mesma chave + payload diferente      → 409 Conflict (contrato quebrado pelo cliente)
 *  - Sem chave                            → deixa passar normalmente
 *
 * Armazenamento em memória (ConcurrentHashMap) — adequado para H2/dev.
 * Em produção substituiria por Redis com TTL.
 */
@Slf4j
@Component
@Order(3)
public class IdempotencyFilter extends OncePerRequestFilter {

    private record CachedResponse(int status, String contentType, String body, String bodyHash) {}

    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        return !method.equals(HttpMethod.POST.name())
                && !method.equals(HttpMethod.PUT.name())
                && !method.equals(HttpMethod.PATCH.name());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String key = request.getHeader("X-Idempotency-Key");

        if (key == null || key.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        // Capturar body da requisição para comparar hash
        ContentCachingRequestWrapper cachedRequest = new ContentCachingRequestWrapper(request);

        // Precisamos ler o body antes de consultar o cache
        // (ContentCachingRequestWrapper lê lazily; forçamos a leitura)
        cachedRequest.getInputStream().readAllBytes();
        String incomingBody  = new String(cachedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
        String incomingHash  = sha256(incomingBody);

        if (cache.containsKey(key)) {
            CachedResponse cached = cache.get(key);

            // CORRIGIDO: payload diferente com mesma chave → 409
            if (!incomingHash.equals(cached.bodyHash())) {
                log.warn("Idempotency conflict — key={} payload changed", key);
                response.setStatus(409);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(String.format(
                        "{\"timestamp\":\"%s\",\"status\":409,\"erro\":\"Conflict\","
                        + "\"mensagem\":\"A chave de idempotência '%s' já foi usada com "
                        + "um payload diferente. Use uma nova chave para uma nova operação.\","
                        + "\"caminho\":\"%s\"}",
                        Instant.now(), key, request.getRequestURI()));
                return;
            }

            // Mesmo payload → retorna resposta cacheada
            log.info("Idempotency hit — key={} status={}", key, cached.status());
            response.setStatus(cached.status());
            response.setContentType(cached.contentType());
            response.getWriter().write(cached.body());
            return;
        }

        // Chave nova → processa e armazena resultado
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        chain.doFilter(cachedRequest, wrappedResponse);

        int    status      = wrappedResponse.getStatus();
        String contentType = wrappedResponse.getContentType();
        String body        = new String(wrappedResponse.getContentAsByteArray(),
                                        wrappedResponse.getCharacterEncoding());

        if (status >= 200 && status < 300) {
            cache.put(key, new CachedResponse(status, contentType, body, incomingHash));
            log.info("Idempotency stored — key={} status={}", key, status);
        }

        wrappedResponse.copyBodyToResponse();
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            return input.hashCode() + "";
        }
    }
}
