package com.myproject.point.security;

import com.myproject.point.config.SecurityProperties;
import com.myproject.point.domain.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecurityProperties securityProperties;
    private final Clock clock;
    private Key signingKey;

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(securityProperties.getJwt().getSecret().getBytes());
    }

    public String generateAccessToken(Member member) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiry = now.plus(securityProperties.getJwt().getAccessTokenValidity());

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .addClaims(Map.of("role", member.getRole().name()))
                .setIssuedAt(toDate(now))
                .setExpiration(toDate(expiry))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(clock.getZone()).toInstant());
    }
}
