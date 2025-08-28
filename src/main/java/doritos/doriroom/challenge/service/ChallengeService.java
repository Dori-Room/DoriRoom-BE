package doritos.doriroom.challenge.service;

import doritos.doriroom.atlas.service.AtlasService;
import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.domain.challenge.ChallengeGroup;
import doritos.doriroom.challenge.domain.challenge.ChallengeReward;
import doritos.doriroom.challenge.domain.userchallenge.ChallengeStatus;
import doritos.doriroom.challenge.domain.userchallenge.UserChallenge;
import doritos.doriroom.challenge.dto.response.ChallengeResponseDto;
import doritos.doriroom.challenge.exception.ChallengeArgumentException;
import doritos.doriroom.challenge.exception.ChallengeNotFoundException;
import doritos.doriroom.challenge.exception.ChallengeStatusException;
import doritos.doriroom.challenge.repository.ChallengeRepository;
import doritos.doriroom.challenge.repository.UserChallengeRepository;
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
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final ItemService itemService;
    private final AtlasService atlasService;

    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> getChallengesByGroup(User user, ChallengeGroup challengeGroup, AreaGroup areaGroup){
        // 요청 조건에 따라 필터링된 과제 리스트
        List<Challenge> challenges = challengeFilterByGroup(challengeGroup, areaGroup);

        // 과제들에 대한 유저의 과제 상태 리스트 -> Map으로 변환
        Map<Long, UserChallenge> userChallengeMap = userChallengeRepository.findByUserAndChallengeInWithFetch(user, challenges).stream()
                .collect(Collectors.toMap(uc -> uc.getChallenge().getId(), uc -> uc));

        // 도전과제 정보와 유저의 진행 상태를 반영하여 반환
        return challenges.stream()
                .map(challenge -> {
                    UserChallenge userProgress = userChallengeMap.get(challenge.getId());
                    return ChallengeResponseDto.of(challenge, userProgress);
                })
                .collect(Collectors.toList());

    }

    @Transactional
    public void claimChallengeReward(User user, Long challengeId){
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("ID " + challengeId + "에 해당하는 도전과제가 없습니다."));

        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new ChallengeNotFoundException("해당 도전과제에 대한 진행 정보가 없습니다."));

        if (userChallenge.getStatus() != ChallengeStatus.WAIT_REWARD) {
            throw new ChallengeStatusException("보상을 받을 수 있는 상태가 아닙니다.");
        }

        // 보상 종류에 따라 지급 처리
        challenge.getRewards().forEach(reward -> {
            switch (reward.getRewardType()) {
                case ITEM -> {
                    if (reward.getRewardItem() != null)
                        itemService.addToInventory(user, reward.getRewardItem());
                }
                case EXP -> {
                    if (challenge.getChallengeGroup() == ChallengeGroup.AREA && reward.getAmount() != null)
                        atlasService.addExpToUserAtlas(user, challenge.getAreaGroup(), reward.getAmount());
                }
                case CREDIT -> {
                    if (reward.getAmount() != null)
                        user.addCredit(reward.getAmount());
                }
            }
        });

        userChallenge.setStatus(ChallengeStatus.COMPLETED); // 완료 상태로 변경
    }

    /* 내부 메서드 */
    private List<Challenge> challengeFilterByGroup(ChallengeGroup challengeGroup, AreaGroup areaGroup){
        if(challengeGroup == null)  // challengeGroup은 필수 파라미터
            throw new ChallengeArgumentException("요청에 challengeGroup 값이 필요합니다.");
        if(challengeGroup == ChallengeGroup.AREA && areaGroup == null)   // challengeGroup==AREA인데, 지역 지정 안하면 예외 던짐
            throw new ChallengeArgumentException("지역 과제을 조회하려면 areaGroup 값이 필요합니다.");

        if(challengeGroup == ChallengeGroup.AREA && areaGroup != null){ // 해당 지역 과제 리스트 반환
            return challengeRepository.findByChallengeGroupAndAreaGroupWithRewards(challengeGroup, areaGroup);
        }
        return challengeRepository.findByChallengeGroupWithRewards(challengeGroup); // 일반 과제 리스트 반환
    }
}
