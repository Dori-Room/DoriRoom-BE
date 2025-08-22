package doritos.doriroom.quiz.dto.requset;

public record QuizCompleteRequestDto( // 완료 처리할 퀴즈 id를 받음]
        Long challengeId // 보상 받기 버튼 클릭하여 요청
) {
}
