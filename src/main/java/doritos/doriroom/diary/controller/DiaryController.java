package doritos.doriroom.diary.controller;

import doritos.doriroom.diary.dto.request.DiaryCreateRequestDto;
import doritos.doriroom.diary.dto.request.DiaryUpdateRequestDto;
import doritos.doriroom.diary.dto.response.DiaryDetailResponseDto;
import doritos.doriroom.diary.dto.response.DiaryResponseDto;
import doritos.doriroom.diary.dto.response.DiaryWritingStatusResponseDto;
import doritos.doriroom.diary.service.DiaryService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="일기 관련", description = "일기 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diary")
public class DiaryController {
    private final DiaryService diaryService;

    @Operation(summary = "일기 작성", description = "일기를 작성합니다.")
    @PostMapping("/create")
    public ApiResponse<DiaryResponseDto> createDiary(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid DiaryCreateRequestDto request){
        DiaryResponseDto response = diaryService.createDiary(user.getUserId(), request);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "일기 수정", description = "일기를 수정합니다.")
    @PutMapping("/{diaryId}")
    public ApiResponse<DiaryResponseDto> updateDiary(
        @AuthenticationPrincipal User user,
        @PathVariable UUID diaryId,
        @RequestBody @Valid DiaryUpdateRequestDto request) {
        DiaryResponseDto response = diaryService.updateDiary(user.getUserId(), diaryId, request);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "일기 삭제", description = "일기를 삭제합니다.")
    @DeleteMapping("/{diaryId}")
    public ApiResponse<Void> deleteDiary(
        @AuthenticationPrincipal User user,
        @PathVariable UUID diaryId) {
        diaryService.deleteDiary(user.getUserId(), diaryId);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "일기 상세 조회", description = "일기 상세 정보를 조회합니다.")
    @GetMapping("/{diaryId}")
    public ApiResponse<DiaryDetailResponseDto> getDiaryDetail(
        @PathVariable UUID diaryId) {
        DiaryDetailResponseDto response = diaryService.getDiaryDetail(diaryId);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "월별 일기 작성 여부 조회", description = "특정 월의 일기 작성 여부를 일별로 조회합니다.")
    @GetMapping("/writing-status")
    public ApiResponse<DiaryWritingStatusResponseDto> getMonthlyWritingStatus(
        @AuthenticationPrincipal User user,
        @RequestParam int year,
        @RequestParam int month) {
        DiaryWritingStatusResponseDto response = diaryService.getDiaryWritingStatus(user.getUserId(), year, month);
        return ApiResponse.ok(response);
    }
}
