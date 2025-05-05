package com.Studio.AIStudio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TokenService {
    private final ConcurrentMap<Long, AtomicInteger> tokenUsage = new ConcurrentHashMap<>();
    private final int maxTokensPerMinute;

    public TokenService(@Value("${limits.tokens.per-minute:1000000}") int maxTokensPerMinute) {
        this.maxTokensPerMinute = maxTokensPerMinute;
    }

    public synchronized boolean wouldExceedLimit(int estimatedTokens) {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        int currentUsage = tokenUsage.getOrDefault(currentMinute, new AtomicInteger(0)).get();
        return currentUsage + estimatedTokens > maxTokensPerMinute;
    }

    public synchronized void trackUsage(int estimatedTokens, int actualTokens) {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        tokenUsage.compute(currentMinute, (key, val) -> {
            if (val == null) return new AtomicInteger(actualTokens);
            val.addAndGet(actualTokens - estimatedTokens);
            return val;
        });
    }

    public int estimateTokens(String text) {
        return (int) (text.length() * 0.25);
    }

    public String getRetryAfter() {
        long currentSecond = Instant.now().getEpochSecond();
        return String.valueOf(60 - (currentSecond % 60));
    }

    @Scheduled(fixedRate = 60_000)
    public void cleanupOldEntries() {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        tokenUsage.keySet().removeIf(minute -> minute < currentMinute - 1);
    }
}