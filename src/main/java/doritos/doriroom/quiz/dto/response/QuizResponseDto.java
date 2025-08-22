package doritos.doriroom.quiz.dto.response;

import doritos.doriroom.quiz.domain.Quiz;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public record QuizResponseDto(// 퀴즈 세트의 정보

        Long challengeId, // 매핑된 도전과제
        String title, // 퀴즈 이름
        List<QuestionResponseDto> questions // 문제들
){
    public static QuizResponseDto from(Quiz quiz) {
        return QuizResponseDto.builder()
                .challengeId(quiz.getChallenge().getId())
                .title(quiz.getTitle())
                .questions(quiz.getQuestions().stream()
                        .map(QuestionResponseDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
