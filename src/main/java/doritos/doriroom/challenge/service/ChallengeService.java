package doritos.doriroom.challenge.service;

import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.domain.challenge.ChallengeGroup;
import doritos.doriroom.challenge.domain.userchallenge.UserChallenge;
import doritos.doriroom.challenge.dto.response.ChallengeResponseDto;
import doritos.doriroom.challenge.excetion.ChallengeArgumentException;
import doritos.doriroom.challenge.repository.ChallengeRepository;
import doritos.doriroom.challenge.repository.UserChallengeRepository;
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

    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> getChallengesByGroup(User user, ChallengeGroup challengeGroup, AreaGroup areaGroup){
        // 요청 조건에 따라 필터링된 과제 리스트
        List<Challenge> challenges = challengeFilterByGroup(challengeGroup, areaGroup);

        // 과제들에 대한 유저의 과제 상태 리스트
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserAndChallengeIn(user, challenges);

        // Map으로 변환
        Map<Long, UserChallenge> userChallengeMap = userChallenges.stream()
                .collect(Collectors.toMap(uc -> uc.getChallenge().getId(), uc -> uc));

        // 도전과제 정보와 유저의 진행 상태를 반영하여 반환
        return challenges.stream()
                .map(challenge -> {
                    UserChallenge userProgress = userChallengeMap.get(challenge.getId());
                    return ChallengeResponseDto.of(challenge, userProgress);
                })
                .collect(Collectors.toList());

    }

    /* 내부 메서드 */
    private List<Challenge> challengeFilterByGroup(ChallengeGroup challengeGroup, AreaGroup areaGroup){
        if(challengeGroup == null)  // challengeGroup은 필수 파라미터
            throw new ChallengeArgumentException("요청에 challengeGroup 값이 필요합니다.");
        if(challengeGroup == ChallengeGroup.AREA && areaGroup == null)   // challengeGroup==AREA인데, 지역 지정 안하면 예외 던짐
            throw new ChallengeArgumentException("지역 과제을 조회하려면 areaGroup 값이 필요합니다.");

        if(challengeGroup == ChallengeGroup.AREA && areaGroup != null){ // 해당 지역 과제 리스트 반환
            return challengeRepository.findByChallengeGroupAndAreaGroup(challengeGroup, areaGroup);
        }
        return challengeRepository.findByChallengeGroup(challengeGroup); // 일반 과제 리스트 반환
    }
}
