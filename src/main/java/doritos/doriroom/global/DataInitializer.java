package doritos.doriroom.global;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.repository.AtlasRepository;
import doritos.doriroom.tourApi.domain.AreaGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final AtlasRepository atlasRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 애플리케이션 시작 시 Atlas 데이터가 없으면 초기 데이터를 생성
        createInitialAtlases();
    }


    private void createInitialAtlases() {
        // AreaGroup Enum의 모든 값을 순회하며 Atlas 객체를 생성
        List<Atlas> initialAtlases = Arrays.stream(AreaGroup.values())
                .map(areaGroup -> Atlas.builder().areaGroup(areaGroup).build())
                .collect(Collectors.toList());

        atlasRepository.saveAll(initialAtlases);
        System.out.println(initialAtlases.size() + "개 지역 Atlas 초기 데이터가 생성되었습니다.");
    }
}