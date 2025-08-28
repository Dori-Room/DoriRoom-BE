package doritos.doriroom.quiz.dto.requset;


import jakarta.validation.constraints.NotNull;

public record QuestionSubmitRequestDto(
        @NotNull Long questionId,
        @NotNull byte submittedAnswer // 유저가 고른 답 번호
){}
