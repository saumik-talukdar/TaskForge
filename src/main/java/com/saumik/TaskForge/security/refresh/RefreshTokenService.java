package com.saumik.TaskForge.security.refresh;

import com.saumik.TaskForge.common.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTtl;

    private static final String RT_PREFIX = "rt:";
    private static final String USER_PREFIX = "rt:user:";

    public String create(UUID userId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = RT_PREFIX + token;
        String userKey = USER_PREFIX + userId;

        redisTemplate.opsForValue()
                .set(tokenKey, userId.toString(), Duration.ofMillis(refreshTtl));

        redisTemplate.opsForSet().add(userKey, token);

        redisTemplate.expire(userKey, Duration.ofMillis(refreshTtl));

        return token;
    }

    public UUID validate(String token) {
        String tokenKey = RT_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(tokenKey);

        if (userId == null) {
            throw new InvalidTokenException("Refresh token expired or invalid");
        }

        return UUID.fromString(userId);
    }

    public void logout(String token) {
        String tokenKey = RT_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(tokenKey);

        if (userId != null) {
            redisTemplate.delete(tokenKey);

            String userKey = USER_PREFIX + userId;
            redisTemplate.opsForSet().remove(userKey, token);
        }
    }

    public void logoutAll(UUID userId) {
        String userKey = USER_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userKey);

        if (tokens != null && !tokens.isEmpty()) {
            Set<String> keysToDelete = tokens.stream()
                    .map(t -> RT_PREFIX + t)
                    .collect(Collectors.toSet());
            redisTemplate.delete(keysToDelete);
        }
        redisTemplate.delete(userKey);
    }
}