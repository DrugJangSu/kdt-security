package com.kdt.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String CLAIM_TYPE = "tokenType";

    private final SecretKey key;

    private final long accessSeconds;

    private final long refreshSeconds;


    public JwtService(@Value("${kdt.jwt.secret}") String secret,
                      @Value("${kdt.jwt.access-seconds}") long accessSeconds,
                      @Value("${kdt.jwt.refresh-Seconds}") long refreshSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessSeconds = accessSeconds;
        this.refreshSeconds = refreshSeconds;
    }
    public String createAccess(String loginId) {
        return compact(loginId, TYPE_ACCESS, this.accessSeconds);
    }

    private String compact(String loginId, String tokenType, long seconds) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(loginId)
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(seconds)))
                .signWith(this.key, Jwts.SIG.HS256)
                .compact();
    }
}
