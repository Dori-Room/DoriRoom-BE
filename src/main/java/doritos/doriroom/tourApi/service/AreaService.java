package doritos.doriroom.tourApi.service;

import doritos.doriroom.tourApi.domain.Area;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.tourApi.exception.AreaNotFoundException;
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

    public List<Area> getAllAreas() {
        return areaRepository.findAll();
    }

    public Area getAreaByCode(Integer code) {
        return areaRepository.findById(code)
            .orElseThrow(AreaNotFoundException::new);
    }

    //AreaGroup에 해당하는 좌표 정보 조회
    public String getAreaPolygonInfo(AreaGroup areaGroup){
        Integer areaCode = areaGroup.getCode();
        Area area = getAreaByCode(areaCode);

        return area.getGeoPolygon();
    }
} 