package com.vibe2guys.backend.auth.service;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginThrottleService {

    private final LoginThrottleProperties properties;
    private final Map<String, AttemptState> emailAttempts = new ConcurrentHashMap<>();
    private final Map<String, AttemptState> ipAttempts = new ConcurrentHashMap<>();

    public LoginThrottleService(LoginThrottleProperties properties) {
        this.properties = properties;
    }

    public OffsetDateTime checkAllowed(String email, String ipAddress) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime blockedUntil = latestActiveBlock(now, emailAttempts.get(normalizeEmail(email)), ipAttempts.get(normalizeIp(ipAddress)));
        if (blockedUntil != null) {
            return blockedUntil;
        }
        cleanupIfExpired(emailAttempts, normalizeEmail(email), now);
        cleanupIfExpired(ipAttempts, normalizeIp(ipAddress), now);
        return null;
    }

    public OffsetDateTime recordFailure(String email, String ipAddress) {
        OffsetDateTime now = OffsetDateTime.now();
        AttemptState emailState = registerFailure(emailAttempts, normalizeEmail(email), now);
        AttemptState ipState = registerFailure(ipAttempts, normalizeIp(ipAddress), now);
        return latestActiveBlock(now, emailState, ipState);
    }

    public void recordSuccess(String email, String ipAddress) {
        emailAttempts.remove(normalizeEmail(email));
        ipAttempts.remove(normalizeIp(ipAddress));
    }

    private AttemptState registerFailure(Map<String, AttemptState> attempts, String key, OffsetDateTime now) {
        return attempts.compute(key, (ignored, current) -> {
            if (current == null) {
                AttemptState state = AttemptState.initial(now);
                if (state.failureCount >= properties.maxAttempts()) {
                    state.blockedUntil = now.plusSeconds(properties.blockSeconds());
                }
                return state;
            }
            AttemptState state = current;
            if (state.blockedUntil != null && state.blockedUntil.isAfter(now)) {
                return state;
            }
            if (state.windowStartedAt.plusSeconds(properties.windowSeconds()).isBefore(now)) {
                state = AttemptState.initial(now);
            } else {
                state.failureCount++;
            }
            if (state.failureCount >= properties.maxAttempts()) {
                state.blockedUntil = now.plusSeconds(properties.blockSeconds());
            }
            return state;
        });
    }

    private OffsetDateTime latestActiveBlock(OffsetDateTime now, AttemptState emailState, AttemptState ipState) {
        OffsetDateTime emailBlockedUntil = emailState != null && emailState.blockedUntil != null && emailState.blockedUntil.isAfter(now)
                ? emailState.blockedUntil
                : null;
        OffsetDateTime ipBlockedUntil = ipState != null && ipState.blockedUntil != null && ipState.blockedUntil.isAfter(now)
                ? ipState.blockedUntil
                : null;
        if (emailBlockedUntil == null) {
            return ipBlockedUntil;
        }
        if (ipBlockedUntil == null) {
            return emailBlockedUntil;
        }
        return emailBlockedUntil.isAfter(ipBlockedUntil) ? emailBlockedUntil : ipBlockedUntil;
    }

    private void cleanupIfExpired(Map<String, AttemptState> attempts, String key, OffsetDateTime now) {
        AttemptState state = attempts.get(key);
        if (state == null) {
            return;
        }
        boolean blockExpired = state.blockedUntil == null || !state.blockedUntil.isAfter(now);
        boolean windowExpired = state.windowStartedAt.plusSeconds(properties.windowSeconds()).isBefore(now);
        if (blockExpired && windowExpired) {
            attempts.remove(key);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeIp(String ipAddress) {
        return ipAddress == null ? "unknown" : ipAddress.trim();
    }

    private static final class AttemptState {
        private int failureCount;
        private OffsetDateTime windowStartedAt;
        private OffsetDateTime blockedUntil;

        private static AttemptState initial(OffsetDateTime now) {
            AttemptState state = new AttemptState();
            state.failureCount = 1;
            state.windowStartedAt = now;
            return state;
        }
    }
}
