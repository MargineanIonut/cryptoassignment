package com.cryptoproject.cryptopricereader.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitingConfig {

    @Value("${rate.limit.tokens}")
    private int tokens;

    @Value("${rate.limit.duration.minutes}")
    private int durationMinutes;

    @Bean
    public Bucket createRateLimiter() {
        Bandwidth limit = Bandwidth.classic(tokens, Refill.greedy(tokens, Duration.ofMinutes(durationMinutes)));
        return Bucket4j.builder().addLimit(limit).build();
    }
}
