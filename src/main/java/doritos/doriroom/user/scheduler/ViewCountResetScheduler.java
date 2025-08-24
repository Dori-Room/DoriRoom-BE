package doritos.doriroom.user.scheduler;

import doritos.doriroom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountResetScheduler {
    private final UserRepository userRepository;

    // 매일 자정(00:00:00)에 모든 유저의 조회수 초기화
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetViewCounts() {
        log.info("조회수 초기화 스케줄러 시작");

        try {
            int resetCount = userRepository.resetAllViewCounts();
            log.info("조회수 초기화 완료: {}명의 유저", resetCount);
        } catch (Exception e) {
            log.error("조회수 초기화 중 오류 발생: {}", e.getMessage(), e);
        }

        log.info("조회수 초기화 스케줄러 완료");
    }
}
