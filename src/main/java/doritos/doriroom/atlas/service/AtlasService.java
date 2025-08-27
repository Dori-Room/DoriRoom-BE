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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** 유저가 지역 도전과제 수행 시 획득한 경험치를 도감에 반영,
     해당 지역에 대해 최초 획득하는 경우 유저 지역 도감을 생성 **/
    @Transactional
    public void addExpToUserAtlas(User user, AreaGroup areaGroup, Long exp) {
        // 원본 지역 도감
        Atlas atlas = atlasRepository.findByAreaGroup(areaGroup)
                .orElseThrow(() -> new AtlasNotFoundException());

        // 유저 지역도감 조회 (또는 생성)
        UserAtlas userAtlas = userAtlasRepository.findByUserAndAtlas(user, atlas)
                .orElseGet(()-> {
                    UserAtlas newUserAtlas = UserAtlas.builder()
                            .user(user)
                            .atlas(atlas)
                            .level(0) // 초기 레벨
                            .currentExp(0L)
                            .build();
                    return userAtlasRepository.save(newUserAtlas);
                });

        // 유저 지역도감에 exp 추가
        userAtlas.setCurrentExp(userAtlas.getCurrentExp() + exp);

        levelUp(userAtlas); // 레벨업 내부 메서드, 조건 만족 시 레벨업 처리
    }


    /* 내부 메서드 */

    // 유저 지역도감 정보를 받아 레벨업 처리
    private void levelUp(UserAtlas userAtlas){
        Long requiredExp = levelPolicy.calculateRequiredExp(userAtlas.getLevel());

        while (userAtlas.getCurrentExp() >= requiredExp) {
            userAtlas.setCurrentExp(userAtlas.getCurrentExp() - requiredExp); //레벨업에 필요한 경험치만큼 차감
            userAtlas.setLevel(userAtlas.getLevel() + 1); // 레벨업
            requiredExp = levelPolicy.calculateRequiredExp(userAtlas.getLevel()); // 다음 필요 경험치 업데이트
        }
    }

}
