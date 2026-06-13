package com.saumik.TaskForge.security.refresh;

import com.saumik.TaskForge.common.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
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
        // 1. Run the lazy cleanup to wipe out any dead tokens for this user before creating a new one
        cleanExpiredTokensLazily(userId);

        String token = UUID.randomUUID().toString();
        String tokenKey = RT_PREFIX + token;
        String userKey = USER_PREFIX + userId;
        Duration ttl = Duration.ofMillis(refreshTtl);

        // 2. Atomically link and save the tokens inside a single Redis Transaction
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi(); // Begin transaction block

                operations.opsForValue().set(tokenKey, userId.toString(), ttl);
                operations.opsForSet().add(userKey, token);

                // Add a small buffer safety window to the index set expiration
                operations.expire(userKey, ttl.plus(Duration.ofDays(1)));

                return operations.exec(); // Execute all commands as one unit
            }
        });

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

    public void logout(UUID userId,String token) {
        String tokenKey = RT_PREFIX + token;
        String storedUserId = redisTemplate.opsForValue().get(tokenKey);

        if (storedUserId != null) {
            // CRITICAL CHECK: Verify the token actually belongs to the user requesting logout
            if (!(UUID.fromString(storedUserId)).equals(userId)) {
                throw new InvalidTokenException("Action unauthorized: Token ownership mismatch.");
            }
            String userKey = USER_PREFIX + userId;
            // Atomically clear individual token data out of both Redis spaces
            redisTemplate.execute(new SessionCallback<List<Object>>() {
                @Override
                @SuppressWarnings("unchecked")
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.delete(tokenKey);
                    operations.opsForSet().remove(userKey, token);
                    return operations.exec();
                }
            });
        }
    }

    public void logoutAll(UUID userId) {
        String userKey = USER_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userKey);

        if (tokens != null && !tokens.isEmpty()) {
            Set<String> keysToDelete = tokens.stream()
                    .map(t -> RT_PREFIX + t)
                    .collect(Collectors.toSet());

            // Add the index track key itself into the list to drop everything in a single trip
            keysToDelete.add(userKey);
            redisTemplate.delete(keysToDelete);
        }
    }

    /**
     * Lazily sweeps through the user's tracking set and evicts any token records
     * that have naturally expired as standalone String values.
     */
    private void cleanExpiredTokensLazily(UUID userId) {
        String userKey = USER_PREFIX + userId;
        Set<String> trackedTokens = redisTemplate.opsForSet().members(userKey);

        if (trackedTokens != null && !trackedTokens.isEmpty()) {
            Set<String> expiredTokens = trackedTokens.stream()
                    .filter(token -> Boolean.FALSE.equals(redisTemplate.hasKey(RT_PREFIX + token)))
                    .collect(Collectors.toSet());

            if (!expiredTokens.isEmpty()) {
                redisTemplate.opsForSet().remove(userKey, expiredTokens.toArray());
            }
        }
    }
}