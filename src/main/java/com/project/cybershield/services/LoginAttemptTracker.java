package com.project.cybershield.services;



public class LoginAttemptTracker {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MS = 5 * 60 * 1000; // 5 minuta

    private final java.util.concurrent.ConcurrentHashMap<String, AttemptRecord> attempts
            = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Provjeri je li user blokiran
     */
    public boolean isBlocked(String username) {
        AttemptRecord record = attempts.get(username);

        if (record == null) return false;

        long now = System.currentTimeMillis();

        // Reset ako je prošao block period
        if (now - record.lastAttemptTime > BLOCK_DURATION_MS) {
            attempts.remove(username);
            return false;
        }

        return record.failedAttempts >= MAX_ATTEMPTS;
    }

    /**
     * Zabilježi neuspjeli pokušaj
     */
    public void recordFailedAttempt(String username) {
        long now = System.currentTimeMillis();

        attempts.compute(username, (key, existing) -> {
            if (existing == null) {
                return new AttemptRecord(1, now);
            }

            // Reset ako je prošao block period
            if (now - existing.lastAttemptTime > BLOCK_DURATION_MS) {
                return new AttemptRecord(1, now);
            }

            return new AttemptRecord(existing.failedAttempts + 1, now);
        });
    }

    /**
     * Zabilježi uspješan login (resetuj attempts)
     */
    public void recordSuccessfulAttempt(String username) {
        attempts.remove(username);
    }

    private static class AttemptRecord {
        final int failedAttempts;
        final long lastAttemptTime;

        AttemptRecord(int failedAttempts, long lastAttemptTime) {
            this.failedAttempts = failedAttempts;
            this.lastAttemptTime = lastAttemptTime;
        }
    }
}