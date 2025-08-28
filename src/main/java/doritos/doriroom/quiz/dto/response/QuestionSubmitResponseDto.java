package doritos.doriroom.quiz.dto.response;

import doritos.doriroom.quiz.domain.Question;
import lombok.Builder;

@Builder
public record QuestionSubmitResponseDto(
        boolean isCorrect, // 정답 여부
        byte correctAnswer, // 실제 정답
        String commentary // 문제 해설
) {
    public static QuestionSubmitResponseDto of(Question question, boolean isCorrect) {
        return QuestionSubmitResponseDto.builder()
                .isCorrect(isCorrect)
                .correctAnswer(question.getCorrectAnswer())
                .commentary(question.getCommentary())
                .build();
    }
}
