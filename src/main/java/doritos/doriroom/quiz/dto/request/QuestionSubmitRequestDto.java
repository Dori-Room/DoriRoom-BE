package doritos.doriroom.quiz.dto.request;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuestionSubmitRequestDto(
        @NotNull @Positive Long questionId,
        @NotNull @Min(1) @Max(4) byte submittedAnswer // 유저가 고른 답 번호
){}
