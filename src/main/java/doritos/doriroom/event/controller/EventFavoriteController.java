package doritos.doriroom.event.controller;

import doritos.doriroom.event.dto.request.EventIdRequestDto;
import doritos.doriroom.event.dto.response.EventResponseDto;
import doritos.doriroom.event.service.EventFavoriteService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UsernameNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name="축제 즐겨찾기 기능", description = "축제 즐겨찾기 관련 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/event/favorite")
public class EventFavoriteController {
    private final EventFavoriteService eventFavoriteService;

    @Operation(summary = "즐겨찾기 버튼", description = "축제 즐겨찾기를 추가 또는 취소")
    @PostMapping("/")
    public ApiResponse<Boolean> toggleFavorite(
        @AuthenticationPrincipal User user,
        @RequestBody EventIdRequestDto request
    ) {
        if (user == null) {
            throw new UsernameNotFoundException();
        }

        boolean isFavorite = eventFavoriteService.toggleFavorite(user, request.eventId());
        return ApiResponse.ok(isFavorite);
    }

    @Operation(summary = "즐겨찾기 상태 확인", description = "특정 축제의 즐겨찾기 상태를 확인")
    @GetMapping("/check")
    public ApiResponse<Boolean> checkLike(
        @AuthenticationPrincipal User user,
        @Parameter(description = "축제ID", required = true)
        @RequestParam("eventId") UUID eventId
    ) {
        if (user == null) {
            throw new UsernameNotFoundException();
        }

        boolean isLiked = eventFavoriteService.isLiked(user, eventId);
        return ApiResponse.ok(isLiked);
    }

    @Operation(summary = "내가 좋아요한 축제 목록", description = "현재 로그인한 사용자가 좋아요한 축제 목록을 조회")
    @GetMapping("/list")
    public ApiResponse<Page<EventResponseDto>> getMyFavoriteEvents(
        @AuthenticationPrincipal User user,
        @ParameterObject Pageable pageable
    ) {
        if (user == null) {
            throw new UsernameNotFoundException();
        }

        Page<EventResponseDto> favorites = eventFavoriteService.getUserFavoriteEvents(user, pageable);
        return ApiResponse.ok(favorites);
    }
}
