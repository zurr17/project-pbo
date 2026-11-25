package com.sarpraskost.app.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Layanan proteksi anti brute-force pada endpoint login.
 * Membatasi percobaan gagal per kombinasi (IP + identifier) dan per IP lintas akun.
 */
@Service
public class LoginAttemptService {

    public static final int MAX_ACCOUNT_ATTEMPTS = 5;
    public static final int MAX_IP_ATTEMPTS = 15;
    public static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(10);
    public static final Duration LOCKOUT_DURATION = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, AttemptRecord> accountAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptRecord> ipAttempts = new ConcurrentHashMap<>();

    public void checkAllowed(String clientIp, String identifier) {
        Instant now = Instant.now();
        String normIp = normalizeIp(clientIp);
        String accountKey = buildAccountKey(normIp, identifier);

        long accountRemainingSec = getRemainingLockSeconds(accountAttempts, accountKey, now);
        if (accountRemainingSec > 0) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Terlalu banyak percobaan login gagal. Akun dikunci sementara selama "
                    + formatRemaining(accountRemainingSec) + "."
            );
        }

        long ipRemainingSec = getRemainingLockSeconds(ipAttempts, normIp, now);
        if (ipRemainingSec > 0) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Terlalu banyak percobaan login gagal dari perangkat Anda. Silakan coba lagi dalam "
                    + formatRemaining(ipRemainingSec) + "."
            );
        }
    }

    /**
     * Mencatat kegagalan login dan mengembalikan sisa percobaan tersisa untuk akun terkait (0 jika terkunci).
     */
    public int recordFailureAndGetRemaining(String clientIp, String identifier) {
        Instant now = Instant.now();
        String normIp = normalizeIp(clientIp);
        String accountKey = buildAccountKey(normIp, identifier);

        AttemptRecord ipRecord = incrementAttempt(ipAttempts, normIp, MAX_IP_ATTEMPTS, now);
        AttemptRecord accRecord = incrementAttempt(accountAttempts, accountKey, MAX_ACCOUNT_ATTEMPTS, now);

        if (ipRecord.isLocked(now) || accRecord.isLocked(now)) {
            return 0;
        }
        return Math.max(0, MAX_ACCOUNT_ATTEMPTS - accRecord.failures());
    }

    public void recordSuccess(String clientIp, String... identifiers) {
        String normIp = normalizeIp(clientIp);
        if (identifiers != null) {
            for (String id : identifiers) {
                if (id != null && !id.trim().isEmpty()) {
                    accountAttempts.remove(buildAccountKey(normIp, id));
                }
            }
        }
        ipAttempts.computeIfPresent(normIp, (k, existing) -> {
            if (existing.isLocked(Instant.now())) {
                return existing;
            }
            return existing.failures() <= 1 ? null : existing.decrement();
        });
    }

    public void clearAll() {
        accountAttempts.clear();
        ipAttempts.clear();
    }

    private AttemptRecord incrementAttempt(
        ConcurrentHashMap<String, AttemptRecord> store,
        String key,
        int threshold,
        Instant now
    ) {
        return store.compute(key, (k, existing) -> {
            if (existing == null || existing.isWindowExpired(now) || existing.hasLockExpired(now)) {
                int count = 1;
                Instant lockedUntil = count >= threshold ? now.plus(LOCKOUT_DURATION) : null;
                return new AttemptRecord(count, now, lockedUntil);
            }
            if (existing.isLocked(now)) {
                return existing;
            }
            int nextCount = existing.failures() + 1;
            Instant lockedUntil = nextCount >= threshold ? now.plus(LOCKOUT_DURATION) : null;
            return new AttemptRecord(nextCount, now, lockedUntil);
        });
    }

    private long getRemainingLockSeconds(ConcurrentHashMap<String, AttemptRecord> store, String key, Instant now) {
        AttemptRecord record = store.get(key);
        if (record == null) {
            return 0;
        }
        if (record.hasLockExpired(now)) {
            store.remove(key, record);
            return 0;
        }
        if (!record.isLocked(now)) {
            return 0;
        }
        return Math.max(1, Duration.between(now, record.lockedUntil()).toSeconds());
    }

    private String formatRemaining(long seconds) {
        if (seconds < 60) {
            return seconds + " detik";
        }
        long mins = (seconds + 59) / 60;
        return mins + " menit";
    }

    private String normalizeIp(String rawIp) {
        if (rawIp == null || rawIp.trim().isEmpty()) {
            return "unknown";
        }
        return rawIp.trim();
    }

    private String buildAccountKey(String normIp, String identifier) {
        String cleanId = identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT);
        return normIp + "|" + cleanId;
    }

    private record AttemptRecord(int failures, Instant lastAttemptAt, Instant lockedUntil) {
        boolean isLocked(Instant now) {
            return lockedUntil != null && now.isBefore(lockedUntil);
        }

        boolean hasLockExpired(Instant now) {
            return lockedUntil != null && !now.isBefore(lockedUntil);
        }

        boolean isWindowExpired(Instant now) {
            return lockedUntil == null && Duration.between(lastAttemptAt, now).compareTo(ATTEMPT_WINDOW) > 0;
        }

        AttemptRecord decrement() {
            return new AttemptRecord(Math.max(0, failures - 1), lastAttemptAt, null);
        }
    }
}
