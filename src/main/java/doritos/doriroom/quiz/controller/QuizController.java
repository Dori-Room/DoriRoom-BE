package doritos.doriroom.quiz.controller;

import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.quiz.dto.requset.QuizCompleteRequestDto;
import doritos.doriroom.quiz.dto.response.QuizCompleteResponseDto;
import doritos.doriroom.quiz.dto.response.QuizResponseDto;
import doritos.doriroom.quiz.service.QuizService;

import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
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
                                                @PathVariable Long challengeId) {
        return ApiResponse.ok(quizService.getQuiz(user, challengeId));
    }

    @PostMapping("/complete")
    @Operation(summary ="보상 받기 를 클릭 -> 퀴즈 완료를 전달하여 과제 완료 처리 및 보상 아이템 수납", description = "requset dto에 완료한 도전과제 ID를 포함하여 요청")
    public ApiResponse<QuizCompleteResponseDto> completeQuiz(@AuthenticationPrincipal User user,
                                                             @RequestBody QuizCompleteRequestDto request) {
        return ApiResponse.ok(quizService.completeQuiz(user, request));
    }

}
