package doritos.doriroom.atlas.service;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.AtlasReward;
import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.atlas.dto.response.AtlasResponseDto;
import doritos.doriroom.atlas.excetion.AtlasNotFoundException;
import doritos.doriroom.atlas.policy.LevelPolicy;
import doritos.doriroom.atlas.repository.AtlasRepository;
import doritos.doriroom.atlas.repository.AtlasRewardRepository;
import doritos.doriroom.atlas.repository.UserAtlasRepository;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtlasService {
    private final AtlasRepository atlasRepository;
    private final UserAtlasRepository userAtlasRepository;
    private final AtlasRewardRepository atlasRewardRepository;
    private final LevelPolicy levelPolicy;

    public List<AtlasResponseDto> getAtlases(User user, AreaGroup areaGroup) {

        // 전체 또는 지역별 도감 조회 (원본 도감)
        List<Atlas> atlases;
        if (areaGroup != null) {
            // areaGroup 파라미터가 있으면, 해당 지역만 조회
            atlases = List.of(atlasRepository.findByAreaGroup(areaGroup)
                    .orElseThrow(() -> new AtlasNotFoundException()));
        } else {
            // areaGroup 파라미터가 없으면, 모든 지역 조회
            atlases = atlasRepository.findAll();    }

        // 유저의 모든 지역별 도감
        List<UserAtlas> userAtlases = userAtlasRepository.findByUser(user);

        // Map으로 변환 (키: 원본 도감의 지역 int값을 따름)
        Map<Long, UserAtlas> userAtlasMap = userAtlases.stream()
                .collect(Collectors.toMap(ua -> ua.getAtlas().getId(), ua -> ua));


        return atlases.stream()
                .map(atlas -> {
                    // 유저 진행 정보 조회 (없으면 초기 레벨 데이터 반환)
                    UserAtlas userProgress = userAtlasMap.get(atlas.getId());
                    int currentLevel = (userProgress != null) ? userProgress.getLevel() : 0;
                    Long currentExp = (userProgress != null) ? userProgress.getCurrentExp() : 0L;

                    // 다음 레벨업에 필요한 경험치 계산
                    Long nextLevelExp = levelPolicy.calculateRequiredExp(currentLevel);

                    // 다음 보상 정보 조회
                    AtlasReward nextRewardItem = atlasRewardRepository
                            .findFirstByAtlasAndTargetLevelGreaterThanOrderByTargetLevelAsc(atlas, currentLevel)
                            .orElse(null); // 보상 아이템 없는 경우 null

                    return AtlasResponseDto.of(atlas, userProgress, nextRewardItem, nextLevelExp);
                })
                .collect(Collectors.toList());
    }

}
