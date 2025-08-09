package doritos.doriroom.guestbook.controller;

import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.guestbook.dto.request.GuestbookRequestDto;
import doritos.doriroom.guestbook.dto.response.GuestbookResponseDto;
import doritos.doriroom.guestbook.service.GuestbookService;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Guestbook", description = "방명록 관련 API")
@RestController
@RequestMapping("/api/guestbooks")
@RequiredArgsConstructor
public class GuestbookController {
    private final GuestbookService guestbookService;

    @PostMapping
    @Operation(summary = "방명록 작성", description = "특정 사용자의 방에 방명록을 작성합니다.")
    public ApiResponse<GuestbookResponseDto> createGuestbook(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid GuestbookRequestDto request) {
        GuestbookResponseDto response = guestbookService.createGuestbook(user.getUserId(), request);
        return ApiResponse.ok(response);
    }

    @GetMapping("/room/{roomOwnerId}")
    @Operation(summary = "방명록 목록 조회", description = "특정 방의 방명록 목록을 최신순으로 조회합니다.")
    public ApiResponse<Page<GuestbookResponseDto>> getGuestbooksByRoom(
            @Parameter(description = "방 주인 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID roomOwnerId,
            @ParameterObject Pageable pageable) {
        Page<GuestbookResponseDto> response = guestbookService.getGuestbooksByRoomOwner(roomOwnerId, pageable);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{guestbookId}")
    @Operation(summary = "방명록 삭제", description = "방명록을 삭제합니다. 작성자 또는 방 주인만 삭제 가능합니다.")
    public ApiResponse<Void> deleteGuestbook(
            @AuthenticationPrincipal User user,
            @Parameter(description = "방명록 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID guestbookId) {
        guestbookService.deleteGuestbook(guestbookId, user);
        return ApiResponse.ok();
    }
} 