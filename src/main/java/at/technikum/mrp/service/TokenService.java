package at.technikum.mrp.service;

import at.technikum.mrp.config.ServerConfig;
import at.technikum.mrp.util.ApiException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenService {

    private static class Session {
        final int userId;
        final Instant expiresAt;
        Session(int userId, Instant expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public String issueToken(int userId) {
        String token = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(ServerConfig.getTokenExpirationHours(), ChronoUnit.HOURS);
        sessions.put(token, new Session(userId, expires));
        return token;
    }

    public int requireUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Missing/invalid Authorization header");
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        Session s = sessions.get(token);
        if (s == null) throw ApiException.unauthorized("Invalid token");
        if (Instant.now().isAfter(s.expiresAt)) {
            sessions.remove(token);
            throw ApiException.unauthorized("Token expired");
        }
        return s.userId;
    }
}
