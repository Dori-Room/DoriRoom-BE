package doritos.doriroom.quiz.dto.response;

import doritos.doriroom.quiz.domain.Question;
import lombok.Builder;

@Builder
public record QuestionResponseDto( // 퀴즈의 문제 정보
        int sequence,
        String content,
        String option1,
        String option2,
        String option3,
        String option4,
        int correctAnswer,
        String commentary
) {
    public static QuestionResponseDto from(Question question) {
        return QuestionResponseDto.builder()
                .sequence(question.getSequence())
                .content(question.getContent())
                .option1(question.getOption1())
                .option2(question.getOption2())
                .option3(question.getOption3())
                .option4(question.getOption4())
                .correctAnswer(question.getCorrectAnswer())
                .commentary(question.getCommentary())
                .build();
    }
}
