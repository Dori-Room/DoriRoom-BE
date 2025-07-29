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
    private static final String SEARCH_KEYWORDS_WEEKLY = "search:keywords:weekly:";
    private static final String SEARCH_KEYWORDS_CURRENT_WEEK = "search:keywords:current_week:";

    //검색어 카운트 증가
    public void incrementSearchCount(String keyword) {
        String currentWeek = getCurrentWeekKey();
        String currentHour = getCurrentHourKey();

        String weeklyKey = SEARCH_KEYWORDS_WEEKLY + currentWeek;
        String hourlyKey = SEARCH_KEYWORDS_CURRENT_WEEK + currentHour;

        // 일주일 전체 카운트 증가
        redisTemplate.opsForZSet().incrementScore(weeklyKey, keyword, 1);

        // 현재 시간대 카운트 증가
        redisTemplate.opsForZSet().incrementScore(hourlyKey, keyword, 1);

        // TTL 설정
        redisTemplate.expire(weeklyKey, java.time.Duration.ofDays(14)); // 2주 보관
        redisTemplate.expire(hourlyKey, java.time.Duration.ofDays(7)); // 1주 보관

        log.debug("검색어 카운트 증가: {} (주: {}, 시간: {})", keyword, currentWeek, currentHour);
    }

    // 일주일 기준 인기 검색어 조회
    public List<SearchRankDto> getTopKeywordsWithRankChange() {
        String currentWeek = getCurrentWeekKey();
        String previousWeek = getPreviousWeekKey();

        String currentWeekKey = SEARCH_KEYWORDS_WEEKLY + currentWeek;
        String previousWeekKey = SEARCH_KEYWORDS_WEEKLY + previousWeek;

        // 현재 주 상위 10개
        Set<ZSetOperations.TypedTuple<Object>> currentTop =
            redisTemplate.opsForZSet().reverseRangeWithScores(currentWeekKey, 0, 9);

        Set<ZSetOperations.TypedTuple<Object>> previousTop =
            redisTemplate.opsForZSet().reverseRangeWithScores(previousWeekKey, 0, 9);

        // 현재 주에 데이터가 없으면 저번주 데이터 사용
        if (currentTop.isEmpty()) {
            log.info("현재 주에 데이터가 없어 저번주 데이터를 사용합니다.");
            currentTop = redisTemplate.opsForZSet().reverseRangeWithScores(previousWeekKey, 0, 9);
        }

        if(currentTop.isEmpty()) {
            log.info("데이터가 없어 기본 인기 검색어 생성");
            return createDefaultKeywords();
        }

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

    // 현재 주 키 (월요일 기준)
    private String getCurrentWeekKey() {
        LocalDate now = LocalDate.now();
        LocalDate monday = now.minusDays(now.getDayOfWeek().getValue() - 1); // 월요일로 조정
        return monday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    // 이전 주 키 (월요일 기준)
    private String getPreviousWeekKey() {
        LocalDate now = LocalDate.now();
        LocalDate monday = now.minusDays(now.getDayOfWeek().getValue() - 1); // 월요일로 조정
        LocalDate previousMonday = monday.minusWeeks(1);
        return previousMonday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String getCurrentHourKey() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH"));
    }

    // 기본 인기 검색어 생성 (데이터가 없을 때)
    private List<SearchRankDto> createDefaultKeywords() {
        return List.of(
            new SearchRankDto("서울 축제", 10L, 1, RankChange.SAME),
            new SearchRankDto("부산 축제", 9L, 2, RankChange.SAME),
            new SearchRankDto("대구 축제", 8L, 3, RankChange.SAME),
            new SearchRankDto("인천 축제", 7L, 4, RankChange.SAME),
            new SearchRankDto("광주 축제", 6L, 5, RankChange.SAME),
            new SearchRankDto("대전 축제", 5L, 6, RankChange.SAME),
            new SearchRankDto("울산 축제", 4L, 7, RankChange.SAME),
            new SearchRankDto("세종 축제", 3L, 8, RankChange.SAME),
            new SearchRankDto("경기 축제", 2L, 9, RankChange.SAME),
            new SearchRankDto("강원 축제", 1L, 10, RankChange.SAME)
        );
    }

    private RankChange calculateRankChange(Integer currentRank, Integer previousRank) {
        if (previousRank == null) {
            return RankChange.SAME;
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