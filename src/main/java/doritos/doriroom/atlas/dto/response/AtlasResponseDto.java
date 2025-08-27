package doritos.doriroom.atlas.dto.response;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.AtlasReward;
import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.tourApi.domain.AreaGroup;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AtlasResponseDto(
        Long atlasId,
        UUID userAtlasId,
        AreaGroup areaGroup, // 지역

        // 레벨 관련 - 사용자의 레벨 진행 상태 표시
        int currentLevel,
        Long currentExp,
        Long nextLevelExp, // 다음 레벨로의 달성 기준

        // 다음에 받을 수 있는 보상 아이템 정보
        AtlasRewardItemDto  nextRewardItem

) {
    public static AtlasResponseDto of(Atlas atlas, UserAtlas userAtlas, AtlasReward nextRewardItem, Long nextLevelExp) {
        // 유저의 지역도감에 대한 레벨 현황, 아직 시작하지 않은 경우 기본 값으로 초기화
        int currentLevel = (userAtlas != null) ? userAtlas.getLevel() : 0;
        long currentExp = (userAtlas != null) ? userAtlas.getCurrentExp() : 0L;

        return AtlasResponseDto.builder()
                .atlasId(atlas.getId()) // 지역 도감 id
                .userAtlasId(userAtlas.getUserAtlasId()) // 유저의 해당 지역 도감 id
                .areaGroup(atlas.getAreaGroup())
                .currentLevel(currentLevel)
                .currentExp(currentExp)
                .nextLevelExp(nextLevelExp)
                .nextRewardItem(AtlasRewardItemDto.from(nextRewardItem)) // 다음 보상 정보 DTO로 변환
                .build();
    }

}
