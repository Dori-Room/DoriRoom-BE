package doritos.doriroom.atlas.repository;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.tourApi.domain.AreaGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AtlasRepository extends JpaRepository<Atlas, Long> {
    Optional<Atlas> findByAreaGroup(AreaGroup areaGroup); // 기본(원본) 지역별 도감 조회 시
}
