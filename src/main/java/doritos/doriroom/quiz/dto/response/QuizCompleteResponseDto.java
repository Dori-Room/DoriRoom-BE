package doritos.doriroom.quiz.dto.response;

import doritos.doriroom.challenge.dto.ChallengeRewardDto;

import java.util.List;

public record QuizCompleteResponseDto(
        boolean success,
//        String message // 필요 시 추가
        List<ChallengeRewardDto> rewards // 해당 과제의 보상 정보
) {
}
