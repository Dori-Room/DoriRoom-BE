package doritos.doriroom.challenge.dto.response;

import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.domain.challenge.ChallengeGroup;
import doritos.doriroom.challenge.domain.challenge.ChallengeType;
import doritos.doriroom.challenge.domain.userchallenge.ChallengeStatus;
import doritos.doriroom.challenge.domain.userchallenge.UserChallenge;
import doritos.doriroom.challenge.dto.ChallengeRewardDto;
import doritos.doriroom.tourApi.domain.AreaGroup;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record ChallengeResponseDto(
        Long challengeId,

        String title,
        String content, // nullable
        LocalDate startDate,
        LocalDate endDate,

        ChallengeGroup challengeGroup,
        AreaGroup areaGroup,
        ChallengeType challengeType,

        int targetCount,
        UUID eventId, // nullable
        List<ChallengeRewardDto> rewards, // nullable

        int currentProgress,
        ChallengeStatus status // 과제 상태에 따라 도전 버튼 활성화 혹은 보상 받기 등 처리
)
{
    public static ChallengeResponseDto of(Challenge challenge, UserChallenge userChallenge) {
        return ChallengeResponseDto.builder()
                .challengeId(challenge.getId())
                .title(challenge.getTitle())
                .content(challenge.getContent())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .challengeGroup(challenge.getChallengeGroup())
                .areaGroup(challenge.getAreaGroup())
                .challengeType(challenge.getChallengeType())
                .targetCount(challenge.getTargetCount())
                .eventId(challenge.getEvent() != null ? challenge.getEvent().getEventId() : null)
                .rewards(challenge.getRewards() == null
                        ? List.of()
                        : challenge.getRewards().stream()
                            .map(ChallengeRewardDto::from)
                            .toList())
                .currentProgress(userChallenge != null ? userChallenge.getCurrentProgress() : 0)
                .status(userChallenge != null ? userChallenge.getStatus() : ChallengeStatus.NOT_STARTED)
                .build();
    }
}
