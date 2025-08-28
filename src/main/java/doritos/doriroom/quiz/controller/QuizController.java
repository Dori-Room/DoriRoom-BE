package doritos.doriroom.quiz.controller;

import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.quiz.dto.requset.QuestionSubmitRequestDto;
import doritos.doriroom.quiz.dto.response.QuestionSubmitResponseDto;
import doritos.doriroom.quiz.dto.response.QuizCompleteResponseDto;
import doritos.doriroom.quiz.dto.response.QuizResponseDto;
import doritos.doriroom.quiz.service.QuizService;

import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/{challengeId}")
    @Operation(summary = "특정 퀴즈 도전과제에 해당하는 문제 목록을 조회", description = "퀴즈와 연결된 도전과제의 challengeId를 받아 퀴즈 문제 목록 반환")
    public ApiResponse<QuizResponseDto> getQuiz(@AuthenticationPrincipal User user,
                                                @Parameter(description = "조회할 퀴즈 도전과제의 ID", example = "2")
                                                @PathVariable Long challengeId) {
        return ApiResponse.ok(quizService.getQuiz(user, challengeId));
    }

    @PostMapping("/submit")
    @Operation(summary ="퀴즈의 문제 하나에 대한 정답을 제출", description = "정답/오답 여부 확인, 실제 정답 확인 및 해설 제공")
    public ApiResponse<QuestionSubmitResponseDto> submitQuestionAnswer(@AuthenticationPrincipal User user,
                                                                       @Valid @RequestBody QuestionSubmitRequestDto request) {
        return ApiResponse.ok(quizService.submitQuestionAnswer(user, request));
    }

    @PostMapping("/{challengeId}/complete")
    @Operation(summary ="퀴즈 완료 처리 요청 (과제의 상태를 보상 대기 상태 WAIT_REWARD로 변경", description = "")
    public ApiResponse<QuizCompleteResponseDto> completeQuiz(@AuthenticationPrincipal User user,
                                                             @Parameter(description = "완료 처리할 퀴즈 도전과제의 ID", example = "2")
                                                             @PathVariable Long challengeId) {
        return ApiResponse.ok(quizService.completeQuiz(user, challengeId));
    }

}
