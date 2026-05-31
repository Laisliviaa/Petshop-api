package com.example.petshopapi.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de idempotência via header X-Idempotency-Key.
 * Aplica-se a POST, PUT e PATCH.
 *
 * - Chave nova                        → processa e armazena resultado
 * - Mesma chave + mesmo payload       → retorna resposta cacheada (sem reprocessar)
 * - Mesma chave + payload diferente   → 409 Conflict
 */
@Slf4j
@Component
@Order(3)
public class IdempotencyFilter extends OncePerRequestFilter {

    private record CachedResponse(int status, String contentType, byte[] body, String bodyHash) {}

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

        // Ler e guardar o body da requisição para permitir releitura posterior
        byte[] bodyBytes = request.getInputStream().readAllBytes();
        String incomingHash = sha256(bodyBytes);

        if (cache.containsKey(key)) {
            CachedResponse cached = cache.get(key);
            if (!incomingHash.equals(cached.bodyHash())) {
                log.warn("Idempotency conflict — key={}", key);
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
            log.info("Idempotency hit — key={} status={}", key, cached.status());
            response.setStatus(cached.status());
            if (cached.contentType() != null) response.setContentType(cached.contentType());
            response.getOutputStream().write(cached.body());
            return;
        }

        // Criar wrapper que permite releitura do body
        CachedBodyRequestWrapper wrappedRequest = new CachedBodyRequestWrapper(request, bodyBytes);

        // Capturar a resposta
        CachedBodyResponseWrapper wrappedResponse = new CachedBodyResponseWrapper(response);
        chain.doFilter(wrappedRequest, wrappedResponse);

        int status = wrappedResponse.getStatus();
        byte[] responseBody = wrappedResponse.getCachedBody();

        if (status >= 200 && status < 300) {
            cache.put(key, new CachedResponse(status, wrappedResponse.getContentType(),
                    responseBody, incomingHash));
            log.info("Idempotency stored — key={} status={}", key, status);
        }

        // Copiar resposta para o response original
        response.setStatus(status);
        if (wrappedResponse.getContentType() != null)
            response.setContentType(wrappedResponse.getContentType());
        response.getOutputStream().write(responseBody);
    }

    private String sha256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input));
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }

    // ── Wrapper que permite releitura do body ─────────────────────────────────
    static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;
        CachedBodyRequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }
        @Override public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                public int read() { return bais.read(); }
                public boolean isFinished() { return bais.available() == 0; }
                public boolean isReady() { return true; }
                public void setReadListener(ReadListener l) {}
            };
        }
        @Override public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }

    // ── Wrapper que captura a resposta ────────────────────────────────────────
    static class CachedBodyResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        private final PrintWriter writer;
        private ServletOutputStream outputStream;

        CachedBodyResponseWrapper(HttpServletResponse response) {
            super(response);
            writer = new PrintWriter(new OutputStreamWriter(buf, StandardCharsets.UTF_8), true);
        }
        @Override public ServletOutputStream getOutputStream() {
            if (outputStream == null) {
                outputStream = new ServletOutputStream() {
                    public void write(int b) { buf.write(b); }
                    public void write(byte[] b, int off, int len) { buf.write(b, off, len); }
                    public boolean isReady() { return true; }
                    public void setWriteListener(WriteListener l) {}
                };
            }
            return outputStream;
        }
        @Override public PrintWriter getWriter() { return writer; }
        public byte[] getCachedBody() { writer.flush(); return buf.toByteArray(); }
    }
}
