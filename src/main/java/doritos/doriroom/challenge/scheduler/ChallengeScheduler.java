package doritos.doriroom.challenge.scheduler;

import doritos.doriroom.challenge.repository.UserChallengeRepository;
import doritos.doriroom.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChallengeScheduler {
    private final ChallengeService challengeService;
    private final UserChallengeRepository userChallengeRepository;

    // 매일 자정에 실행하여 만료된 도전과제들을 처리
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireChallenges() {
        log.info("도전과제 만료 처리 스케줄러 시작 - 처리 날짜: {}", LocalDate.now().minusDays(1));

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            int expiredCount = challengeService.expireChallenges(LocalDate.now().minusDays(1));

            log.info("도전과제 만료 처리 완료 - {}건", expiredCount);
        } catch (Exception e) {
            log.error("도전과제 만료 처리 중 오류 발생", e);
            throw e; // 재발생시켜 스케줄러가 실패를 인식하도록
        }
    }
}