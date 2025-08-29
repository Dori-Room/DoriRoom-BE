package doritos.doriroom.challenge.service;

import doritos.doriroom.atlas.service.AtlasService;
import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.domain.challenge.ChallengeGroup;
import doritos.doriroom.challenge.domain.challenge.ChallengeType;
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

import java.time.LocalDate;
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

    // endDate가 지난 도전과제 상태를 EXPIRED로 변경, 스케줄러에서 호출
    @Transactional
    public int expireChallenges(LocalDate untilDate) {
        return userChallengeRepository.expireChallenges(
                untilDate, ChallengeStatus.EXPIRED,
                List.of(ChallengeStatus.NOT_STARTED, ChallengeStatus.IN_PROGRESS)
        );
    }

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

    @Transactional
    public void startChallenge(User user, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(ChallengeNotFoundException::new);

        validateChallengeType(challenge); // 해당 도전과제가 수동 상태 변경이 요구되는 과제인지 확인

        // UserChallenge 조회, 없으면 생성
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseGet(() -> userChallengeRepository.save(
                        UserChallenge.builder()
                                .user(user)
                                .challenge(challenge)
                                .status(ChallengeStatus.NOT_STARTED)
                                .build()
                ));

        // 미시작 상태가 아닌 경우 예외 처리
        if (userChallenge.getStatus() != ChallengeStatus.NOT_STARTED) {
            throw new ChallengeStatusException("이미 시작했거나 완료한 과제입니다.");
        }
        userChallenge.setStatus(ChallengeStatus.IN_PROGRESS); // 미시작 과제 -> 도전 중으로 변경
    }

    @Transactional
    public void completeChallenge(User user, Long challengeId) { // 수동으로 보상 대기 상태로 전환 시 사용(축제 관련 도전과제가 해당)
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallengeId(user, challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("해당 도전과제에 대한 진행 정보가 없습니다."));

        // 도전 중 상태인 경우만 보상 대기 상태로 변경 가능
        if (userChallenge.getStatus() != ChallengeStatus.IN_PROGRESS) {
            throw new ChallengeStatusException("도전 중인 과제만 완료 처리할 수 있습니다.");
        }
        userChallenge.setStatus(ChallengeStatus.WAIT_REWARD); // 도전 중 -> 보상 대기 상태로 변경
    }

    @Transactional
    public void updateChallengeProgress(User user, ChallengeType challengeType, int count){ // 반자동으로 진척도 집계하여 상태를 (진행 중 -> 보상 대기) 전환 처리
        List<Challenge> challenges = challengeRepository.findByChallengeType(challengeType); // 같은 타입의 도전과제들

        // 유저의 모든 도전과제 진행 상태 조회 (도전과제 함께 조회)
        Map<Long, UserChallenge> userChallenges = userChallengeRepository.findByUserAndChallengeInWithFetch(user, challenges).stream()
                .collect(Collectors.toMap(uc -> uc.getChallenge().getId(), uc->uc));

        for (Challenge challenge : challenges) {
            UserChallenge userChallenge = userChallenges.get(challenge.getId()); // 유저의 특정 과제에 대한 상태 조회

            // UserChallenge가 없으면 새로 생성
            if (userChallenge == null) {
                userChallenge = UserChallenge.builder()
                        .user(user)
                        .challenge(challenge)
                        .status(ChallengeStatus.NOT_STARTED)
                        .currentProgress(0)
                        .build();
                userChallengeRepository.save(userChallenge);
            }

            // 완료된 과제는 제외
            if (userChallenge.getStatus() == ChallengeStatus.COMPLETED) {
                continue;
            }

            // 현재 과제 진척도 값 (새로운 과제 진척도를 반영한 값)
            int currentProgress = userChallenge.getCurrentProgress() + count;
            userChallenge.setCurrentProgress(Math.max(0, currentProgress)); // 0 미만으로 내려가지 않도록 방지

            // 과제 진척도에 따른 과제 상태 변환
            if (currentProgress >= challenge.getTargetCount()){
                userChallenge.setStatus(ChallengeStatus.WAIT_REWARD); // 현재 진척도가 TargetCount보다 크면 보상 대기
            } else if (currentProgress > 0){ // TargetCount보다 작으면 IN_PROGRESS로 변경 (보상 받지 않았는데 이후 진척도가 줄면 재달성 요구함)
                userChallenge.setStatus(ChallengeStatus.IN_PROGRESS);
            } else { // 진척도가 0이면 미시작 상태로 지정함
                userChallenge.setStatus(ChallengeStatus.NOT_STARTED);
            }

        }
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

    private void validateChallengeType(Challenge challenge) {
        // 수동 시작이 가능한 과제 타입 목록 (도전 버튼 클릭으로 수행하는 과제)
        List<ChallengeType> manualStartTypes = List.of(
                ChallengeType.VISIT_EVENT,
                ChallengeType.REGIONAL_QUIZ
        );

        // 그 이외 자동 집계 도전과제인 경우에는 예외 처리
        if (!manualStartTypes.contains(challenge.getChallengeType())) {
            throw new ChallengeStatusException("수동으로 시작할 수 없는 타입의 과제입니다.");
        }
    }
}
