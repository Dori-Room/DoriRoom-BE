package doritos.doriroom.tourApi.scheduler;

import doritos.doriroom.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TourApiScheduler {

    private final EventService eventService;

    //DB에 수정날짜 어제인 데이터 upsert 추후 스케줄러 시간 변경 필요
    @Scheduled(cron = "0 0 6 * * ?") //매일 새벽 6시 호출
    public void updateEventSchedule(){
        eventService.updateTodayEvents();
    }

}
