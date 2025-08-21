package doritos.doriroom.diary.controller;

import doritos.doriroom.diary.dto.request.DiaryLikeRequestDto;
import doritos.doriroom.diary.service.DiaryLikeService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="일기 좋아요 기능", description = "일기 좋아요 관련 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/diary/like")
class DiaryLikeController {
    private final DiaryLikeService diaryLikeService;

    @Operation(summary = "좋아요 버튼", description = "일기 좋아요를 추가 또는 취소")
    @PostMapping
    public ApiResponse<Boolean> toggleLike(
        @AuthenticationPrincipal User user,
        @RequestBody DiaryLikeRequestDto request
    ) {
        if (user == null) {
            throw new UserNotFoundException();
        }

        boolean isLiked = diaryLikeService.setLikeStatus(user, request.diaryId(), request.isLiked());
        return ApiResponse.ok(isLiked);
    }

    @Operation(summary = "좋아요 상태 확인", description = "특정 일기의 좋아요 상태를 확인")
    @GetMapping("/check/{diaryId}")
    public ApiResponse<Boolean> checkLike(
        @AuthenticationPrincipal User user,
        @Parameter(description = "일기ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        @PathVariable("diaryId") UUID diaryId
    ) {
        if (user == null) {
            throw new UserNotFoundException();
        }

        boolean isLiked = diaryLikeService.isLiked(user, diaryId);
        return ApiResponse.ok(isLiked);
    }
}
