package doritos.doriroom.quiz.dto.requset;


public record QuestionSubmitRequestDto(
        Long questionId,
        byte submittedAnswer // 유저가 고른 답 번호
){}
