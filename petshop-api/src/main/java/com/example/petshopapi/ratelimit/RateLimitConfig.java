package com.example.petshopapi.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    private static final int  READ_CAPACITY  = 10;
    private static final int  WRITE_CAPACITY = 5;
    private static final long WINDOW_SECONDS = 30;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip, boolean isWrite) {
        String key = ip + ":" + (isWrite ? "W" : "R");
        return buckets.computeIfAbsent(key, k -> buildBucket(isWrite));
    }

    private Bucket buildBucket(boolean write) {
        int capacity = write ? WRITE_CAPACITY : READ_CAPACITY;
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofSeconds(WINDOW_SECONDS))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
