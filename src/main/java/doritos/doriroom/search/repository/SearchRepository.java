package doritos.doriroom.search.repository;

import doritos.doriroom.search.dto.RankChange;
import doritos.doriroom.search.dto.SearchRankDto;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchRepository {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SEARCH_KEYWORDS_TODAY = "search:keywords:today:";
    private static final String SEARCH_KEYWORDS_HOURLY = "search:keywords:hourly:";

    //검색어 카운트 증가
    public void incrementSearchCount(String keyword) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String currentHour = getCurrentHourKey();

        String todayKey = SEARCH_KEYWORDS_TODAY + today;
        String hourlyKey = SEARCH_KEYWORDS_HOURLY + currentHour;

        // 오늘 전체 카운트 증가
        redisTemplate.opsForZSet().incrementScore(todayKey, keyword, 1);

        // 현재 시간대 카운트 증가
        redisTemplate.opsForZSet().incrementScore(hourlyKey, keyword, 1);

        // TTL 설정
        redisTemplate.expire(todayKey, java.time.Duration.ofDays(7));
        redisTemplate.expire(hourlyKey, java.time.Duration.ofHours(24));
    }

    public List<SearchRankDto> getTopKeywordsWithRankChange() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String currentHour = getCurrentHourKey();
        String previousHour = getPreviousHourKey();

        String todayKey = SEARCH_KEYWORDS_TODAY + today;
        String currentHourKey = SEARCH_KEYWORDS_HOURLY + currentHour;
        String previousHourKey = SEARCH_KEYWORDS_HOURLY + previousHour;

        // 현재 시간대 상위 10개
        Set<ZSetOperations.TypedTuple<Object>> currentTop =
            redisTemplate.opsForZSet().reverseRangeWithScores(currentHourKey, 0, 9);

        // 현재 시간대 데이터가 없으면 오늘 전체 데이터 사용
        if (currentTop.isEmpty() || currentTop.size() < 10) {
            currentTop = redisTemplate.opsForZSet().reverseRangeWithScores(todayKey, 0, 9);
        }

        // 이전 시간대 상위 10개
        Set<ZSetOperations.TypedTuple<Object>> previousTop =
            redisTemplate.opsForZSet().reverseRangeWithScores(previousHourKey, 0, 9);

        // 순위 변화 계산
        Map<String, Integer> previousRanks = new HashMap<>();
        AtomicInteger rank = new AtomicInteger(1);
        previousTop.forEach(tuple -> previousRanks.put((String) tuple.getValue(), rank.getAndIncrement()));

        // 결과 생성
        AtomicInteger currentRank = new AtomicInteger(1);
        return currentTop.stream()
            .map(tuple -> {
                String keyword = (String) tuple.getValue();
                Long currentCount = tuple.getScore().longValue();
                Integer prevRank = previousRanks.get(keyword);
                Integer currRank = currentRank.getAndIncrement();

                RankChange change = calculateRankChange(currRank, prevRank);

                return new SearchRankDto(keyword, currentCount, currRank, change);
            })
            .toList();
    }

    private String getCurrentHourKey() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH"));
    }

    private String getPreviousHourKey() {
        return LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH"));
    }

    private RankChange calculateRankChange(Integer currentRank, Integer previousRank) {
        if (previousRank == null) {
            return RankChange.UP;
        }

        if (currentRank < previousRank) {
            return RankChange.UP;
        } else if (currentRank > previousRank) {
            return RankChange.DOWN;
        } else {
            return RankChange.SAME;
        }
    }
} 