package doritos.doriroom.ranking.scheduler;

import doritos.doriroom.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingSyncScheduler {
    
    private final RankingService rankingService;
    
    // 매일 새벽 1시에 MySQL과 Redis 동기화
    @Scheduled(cron = "0 0 1 * * *")
    public void syncRankingData() {
        log.info("랭킹 데이터 동기화 작업을 시작합니다.");
        
        try {
            // Redis 랭킹 데이터 초기화 (MySQL 데이터로 동기화)
            rankingService.initializeRankingData();
            
            log.info("랭킹 데이터 동기화 작업이 완료되었습니다.");
        } catch (Exception e) {
            log.error("랭킹 데이터 동기화 작업 중 오류 발생", e);
        }
    }
} 