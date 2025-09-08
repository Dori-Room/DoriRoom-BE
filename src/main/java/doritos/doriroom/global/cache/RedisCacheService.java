package doritos.doriroom.global.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public static final String POPULAR_EVENTS_KEY = "popular_events";
    public static final String UPCOMING_EVENTS_KEY = "upcoming_events";
    public static final String ENDING_SOON_EVENTS_KEY = "ending_soon_events";
    public static final String POPULAR_DIARIES_KEY = "popular_diaries";
    public static final String ALL_ITEMS_KEY = "all_items";
    public static final String CHALLENGES_KEY = "challenges:";

    public static final Duration POPULAR_EVENTS_TTL = Duration.ofMinutes(30);
    public static final Duration UPCOMING_EVENTS_TTL = Duration.ofDays(1);
    public static final Duration ENDING_SOON_EVENTS_TTL = Duration.ofDays(1);
    public static final Duration POPULAR_DIARIES_TTL = Duration.ofMinutes(30);
    public static final Duration ALL_ITEM_TTL = Duration.ofDays(1);
    public static final Duration CHALLENGES_TTL = Duration.ofHours(1);

    //캐시 저장
    public <T> void setCache(String key, T data, Duration ttl) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, jsonData, ttl);
            log.info("캐시 저장 성공: {}", key);
        } catch (Exception e) {
            log.error("캐시 저장 실패: {}", key, e);
        }
    }

    // 리스트 캐시 조회
    public <T> Optional<List<T>> getCacheList(String key, TypeReference<List<T>> typeReference) {
        try {
            Object cachedData = redisTemplate.opsForValue().get(key);
            if (cachedData != null) {
                List<T> result = objectMapper.readValue(cachedData.toString(), typeReference);
                log.info("캐시 조회 성공: {}", key);
                return Optional.of(result);
            }
            log.info("캐시 조회 정보 없음: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("캐시 조회 실패: {}", key, e);
            return Optional.empty();
        }
    }

    // 캐시 삭제
    public void deleteCache(String key) {
        try {
            redisTemplate.delete(key);
            log.info("캐시 삭제: {}", key);
        } catch (Exception e) {
            log.error("캐시 삭제 실패: {}", key, e);
        }
    }

    // 모든 캐시 삭제 (관리자용)
    public void clearAllCaches() {
        try {
            redisTemplate.delete(List.of(
                POPULAR_EVENTS_KEY,
                UPCOMING_EVENTS_KEY,
                ENDING_SOON_EVENTS_KEY,
                POPULAR_DIARIES_KEY,
                ALL_ITEMS_KEY
            ));
            // challenge 캐시 삭제
            Set<String> challengeKeys = redisTemplate.keys(CHALLENGES_KEY + "*");
            if  (challengeKeys != null && !challengeKeys.isEmpty()) {
                redisTemplate.delete(challengeKeys);
            }

            log.info("All caches cleared successfully");
        } catch (Exception e) {
            log.error("Failed to clear all caches", e);
        }
    }
}
