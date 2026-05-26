package com.petshop.api.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia buckets de rate limiting por IP com limites distintos para leitura e escrita.
 *
 * <table border="1">
 * <tr><th>Operação</th><th>Limite</th><th>Janela</th></tr>
 * <tr><td>GET (leitura)</td><td>30 req</td><td>1 minuto</td></tr>
 * <tr><td>POST/PUT/PATCH/DELETE (escrita)</td><td>10 req</td><td>1 minuto</td></tr>
 * </table>
 */
@Configuration
public class RateLimitConfig {

    private static final int  READ_CAPACITY     = 30;
    private static final long READ_REFILL_SECS  = 60;

    private static final int  WRITE_CAPACITY    = 10;
    private static final long WRITE_REFILL_SECS = 60;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Retorna (ou cria) o bucket do IP informado para o tipo de operação.
     *
     * @param ip      IP do cliente
     * @param isWrite {@code true} para POST/PUT/PATCH/DELETE, {@code false} para GET
     */
    public Bucket resolveBucket(String ip, boolean isWrite) {
        String key = ip + ":" + (isWrite ? "W" : "R");
        return buckets.computeIfAbsent(key, k -> buildBucket(isWrite));
    }

    private Bucket buildBucket(boolean write) {
        Bandwidth limit = write
                ? Bandwidth.builder()
                        .capacity(WRITE_CAPACITY)
                        .refillGreedy(WRITE_CAPACITY, Duration.ofSeconds(WRITE_REFILL_SECS))
                        .build()
                : Bandwidth.builder()
                        .capacity(READ_CAPACITY)
                        .refillGreedy(READ_CAPACITY, Duration.ofSeconds(READ_REFILL_SECS))
                        .build();

        return Bucket.builder().addLimit(limit).build();
    }
}
