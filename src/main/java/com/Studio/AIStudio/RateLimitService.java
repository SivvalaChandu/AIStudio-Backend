package com.Studio.AIStudio;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {
    private final Bucket bucket;

    public RateLimitService() {
        this.bucket = Bucket.builder()
                .addLimit(Bandwidth.classic(15, Refill.intervally(15, Duration.ofMinutes(1))))
                .addLimit(Bandwidth.classic(1500, Refill.intervally(1500, Duration.ofDays(1))))
                .build();
    }

    public boolean isRateLimited() {
        return !bucket.tryConsume(1);
    }
}