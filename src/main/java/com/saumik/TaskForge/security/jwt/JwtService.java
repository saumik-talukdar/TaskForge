package com.saumik.TaskForge.security.jwt;

import com.saumik.TaskForge.common.exception.InvalidTokenException;
import com.saumik.TaskForge.common.exception.TokenExpiredException;
import com.saumik.TaskForge.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiry;

    public String generateAccessToken(UUID id, int ver) {
        return Jwts.builder()
                .subject(id.toString())
                .claim("ver", ver)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("JWT expired");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid JWT");
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public boolean isValid(String token, User user) {
        Claims claims = extractAllClaims(token);

        Object verObj = claims.get("ver");
        int tokenVersion = (verObj instanceof Integer)
                ? (Integer) verObj
                : ((Number) verObj).intValue();

        return claims.getSubject().equals(user.getId().toString())
                && tokenVersion == user.getPasswordVersion();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
