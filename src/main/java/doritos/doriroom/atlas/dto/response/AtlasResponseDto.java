package doritos.doriroom.atlas.dto.response;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.AtlasReward;
import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.atlas.domain.UserAtlasReward;
import doritos.doriroom.atlas.dto.AtlasRewardDto;
import doritos.doriroom.tourApi.domain.AreaGroup;
import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record AtlasResponseDto(
        Long atlasId,
        UUID userAtlasId,
        AreaGroup areaGroup, // 지역

        // 레벨 관련 - 사용자의 레벨 진행 상태 표시
        int currentLevel,
        Long currentExp,
        Long nextLevelExp, // 다음 레벨로의 달성 기준 (calculateRequiredExp()의 반환 값)

        // 다음에 받을 수 있는 보상 아이템 정보
        AtlasRewardDto nextRewardItem,
        // 유저가 받을 수 있는 지역도감 보상 리스트
        List<AtlasRewardDto> claimableRewardItems

) {
    public static AtlasResponseDto of(Atlas atlas, UserAtlas userAtlas, AtlasReward nextRewardItem,
                                      Long nextLevelExp, List<AtlasReward> availableRewards, List<UserAtlasReward> userClaimedRewards) {
        // 유저의 지역도감에 대한 레벨 현황, 아직 시작하지 않은 경우 기본 값으로 초기화
        int currentLevel = (userAtlas != null) ? userAtlas.getLevel() : 0;
        long currentExp = (userAtlas != null) ? userAtlas.getCurrentExp() : 0L;
        UUID userAtlasId = (userAtlas != null) ? userAtlas.getUserAtlasId() : null; // 유저의 보상 수령 이력 확인

        // 유저가 이미 받은 보상 아이템들
        Set<Long> claimedRewardIds = userClaimedRewards.stream()
                .map(ucr -> ucr.getAtlasReward().getId())
                .collect(Collectors.toSet());

        // 유저가 받을 수 있는 보상 아이템들
        List<AtlasRewardDto> claimableRewardItems = availableRewards.stream()
                .filter(reward -> reward.getTargetLevel() <= currentLevel) // 레벨 조건 만족
                .filter(reward -> !claimedRewardIds.contains(reward.getId())) // 아직 안 받은 보상들만
                .map(reward -> AtlasRewardDto.of(reward, false)) // isClaimed == false로 설정
                .collect(Collectors.toList());

        return AtlasResponseDto.builder()
                .atlasId(atlas.getId()) // 지역 도감 id
                .userAtlasId(userAtlas.getUserAtlasId()) // 유저의 해당 지역 도감 id
                .areaGroup(atlas.getAreaGroup())
                .currentLevel(currentLevel)
                .currentExp(currentExp)
                .nextLevelExp(nextLevelExp)
                .nextRewardItem(AtlasRewardDto.of(nextRewardItem, false)) // 다음 보상 정보 DTO로 변환
                .claimableRewardItems(claimableRewardItems) // isClaimed 포함
                .build();
    }

}
