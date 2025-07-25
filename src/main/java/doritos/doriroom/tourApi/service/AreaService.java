package doritos.doriroom.tourApi.service;

import doritos.doriroom.tourApi.domain.Area;
import doritos.doriroom.tourApi.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AreaService {
    private final AreaRepository areaRepository;

    @Transactional
    public void initializeAreas() {
        if (areaRepository.count() > 0) {
            log.info("지역 정보가 이미 초기화되어 있습니다.");
            return;
        }

        List<Area> areas = List.of(
            Area.builder().code(1).name("서울").build(),
            Area.builder().code(2).name("인천").build(),
            Area.builder().code(3).name("대전").build(),
            Area.builder().code(4).name("대구").build(),
            Area.builder().code(5).name("광주").build(),
            Area.builder().code(6).name("부산").build(),
            Area.builder().code(7).name("울산").build(),
            Area.builder().code(8).name("세종").build(),
            Area.builder().code(9).name("경기").build(),
            Area.builder().code(32).name("강원").build(),
            Area.builder().code(33).name("충북").build(),
            Area.builder().code(34).name("충남").build(),
            Area.builder().code(35).name("경북").build(),
            Area.builder().code(36).name("경남").build(),
            Area.builder().code(37).name("전북").build(),
            Area.builder().code(38).name("전남").build(),
            Area.builder().code(39).name("제주").build()
        );

        areaRepository.saveAll(areas);
        log.info("지역 정보 초기화 완료: {}개 지역", areas.size());
    }

    public List<Area> getAllAreas() {
        return areaRepository.findAll();
    }

    public Area getAreaByCode(Integer code) {
        return areaRepository.findById(code)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역 코드: " + code));
    }
} 