package doritos.doriroom.atlas.service;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.AtlasReward;
import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.atlas.domain.UserAtlasReward;
import doritos.doriroom.atlas.dto.request.ClaimAtlasRewardRequestDto;
import doritos.doriroom.atlas.dto.response.AtlasResponseDto;
import doritos.doriroom.atlas.excetion.AtlasNotFoundException;
import doritos.doriroom.atlas.excetion.AtlasRewardAlreadyClaimedException;
import doritos.doriroom.atlas.excetion.InsufficientAtlasLevelException;
import doritos.doriroom.atlas.policy.LevelPolicy;
import doritos.doriroom.atlas.repository.AtlasRepository;
import doritos.doriroom.atlas.repository.AtlasRewardRepository;
import doritos.doriroom.atlas.repository.UserAtlasRepository;
import doritos.doriroom.atlas.repository.UserAtlasRewardRepository;
import doritos.doriroom.item.service.ItemService;
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
    private final UserAtlasRewardRepository userAtlasRewardRepository;
    private final LevelPolicy levelPolicy;
    private final ItemService itemService;

    // 전체 또는 지역별 도감 조회
    @Transactional(readOnly = true)
    public List<AtlasResponseDto> getAtlases(User user, AreaGroup areaGroup) {

        // 전체 또는 지역별 도감 리스트 (원본 도감)
        List<Atlas> atlases;
        if (areaGroup != null) {
            // areaGroup 파라미터가 있으면, 해당 지역만 조회
            atlases = List.of(atlasRepository.findByAreaGroup(areaGroup)
                    .orElseThrow(() -> new AtlasNotFoundException()));
        } else {
            // areaGroup 파라미터가 없으면, 모든 지역 조회
            atlases = atlasRepository.findAll();    }


        // 유저의 모든 지역별 도감
        Map<Long, UserAtlas> userAtlasMap = userAtlasRepository.findByUser(user).stream()
                .collect(Collectors.toMap(ua -> ua.getAtlas().getId(), ua -> ua));

        // 유저가 받은 모든 보상 아이템 조회
        Map<Long, List<UserAtlasReward>> userRewardsByAtlas = userAtlasRewardRepository.findByUser(user).stream()
                .collect(Collectors.groupingBy(uar -> uar.getAtlasReward().getAtlas().getId()));

        // 원본 도감의 atlasId 추출
        List<Long> atlasIds = atlases.stream()
                .map(Atlas::getId)
                .collect(Collectors.toList());

        // 모든 Atlas별 보상 아이템들 조회
        Map<Long, List<AtlasReward>> availableRewardsByAtlas = atlasRewardRepository
                .findByAtlasIdInOrderByTargetLevel(atlasIds).stream()
                .collect(Collectors.groupingBy(ar -> ar.getAtlas().getId()));

        return atlases.stream()
                .map(atlas -> {
                    // 유저 진행 정보 조회 (없으면 초기 레벨 데이터 반환)
                    UserAtlas userProgress = userAtlasMap.get(atlas.getId());
                    int currentLevel = (userProgress != null) ? userProgress.getLevel() : 0;
                    Long currentExp = (userProgress != null) ? userProgress.getCurrentExp() : 0L;

                    // 다음 레벨업에 필요한 경험치 계산
                    Long nextLevelExp = levelPolicy.calculateRequiredExp(currentLevel);

                    // 해당 Atlas의 모든 보상 아이템들
                    List<AtlasReward> availableRewards = availableRewardsByAtlas.getOrDefault(atlas.getId(), List.of());

                    // 다음 보상 정보 조회
                    AtlasReward nextRewardItem = availableRewards.stream()
                            .filter(reward -> reward.getTargetLevel() > currentLevel)
                            .findFirst()
                            .orElse(null); // 보상 아이템 없는 경우 null

                    // 유저가 해당 지역에서 이미 받은 보상 아이템들
                    List<UserAtlasReward> userClaimedRewards = userRewardsByAtlas.getOrDefault(atlas.getId(), List.of());

                    return AtlasResponseDto.of(atlas, userProgress, nextRewardItem, nextLevelExp, availableRewards, userClaimedRewards);
                })
                .collect(Collectors.toList());
    }


    /* 유저가 지역 도전과제 수행 시 획득한 경험치를 도감에 반영,
     해당 지역에 대해 최초 획득하는 경우 유저 지역 도감을 생성 */
    @Transactional
    public void addExpToUserAtlas(User user, AreaGroup areaGroup, Long exp) {
        // 원본 지역 도감
        Atlas atlas = atlasRepository.findByAreaGroup(areaGroup)
                .orElseThrow(() -> new AtlasNotFoundException());

        // 유저 지역도감 조회 (또는 생성)
        UserAtlas userAtlas = userAtlasRepository.findByUserAndAtlasWithLock(user, atlas)
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


    // 지역 도감 보상 아이템 수령 처리
    @Transactional
    public void claimAtlasReward(User user, ClaimAtlasRewardRequestDto request) {
        // 수령할 보상 아이템의 정보 조회
        AtlasReward atlasReward = atlasRewardRepository.findById(request.atlasRewardId())
                .orElseThrow(() -> new AtlasNotFoundException("해당 도감 보상 아이템을 찾을 수 없습니다. ID: " + request.atlasRewardId()));

        // 이미 받은 아이템인지 확인
        if (userAtlasRewardRepository.existsByUserAndAtlasReward_Id(user, request.atlasRewardId())) {
            throw new AtlasRewardAlreadyClaimedException();
        }

        // 유저의 해당 지역 도감 정보 조회
        UserAtlas userAtlas = userAtlasRepository.findByUserAndAtlas(user, atlasReward.getAtlas())
                .orElseThrow(() -> new AtlasNotFoundException("유저의 도감 정보를 찾을 수 없습니다."));

        // 레벨 조건 확인
        if (userAtlas.getLevel() < atlasReward.getTargetLevel()) {
            throw new InsufficientAtlasLevelException();
        }

        // 보상 수령 처리 (유저 지역 도감 보상 엔티티에 추가)
        UserAtlasReward userAtlasReward = UserAtlasReward.claimedReward(user, atlasReward);
        userAtlasRewardRepository.save(userAtlasReward);

        itemService.addToInventory(user, atlasReward.getRewardItem());
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
