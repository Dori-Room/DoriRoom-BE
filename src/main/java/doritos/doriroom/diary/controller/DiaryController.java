package doritos.doriroom.diary.controller;

import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.diary.dto.request.*;
import doritos.doriroom.diary.dto.response.*;
import doritos.doriroom.diary.service.DiaryService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.s3.S3Uploader;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name="일기 관련", description = "일기 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diary")
public class DiaryController {
    private final DiaryService diaryService;
    private final S3Uploader s3Uploader;
    private final UserRepository userRepository;

    @Operation(summary = "일기 작성", description = "일기를 작성합니다. '\n' 자세한 내용은 노션 참고해주세요. ")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DiaryResponseDto> createDiary(
        @AuthenticationPrincipal User user,
        @RequestPart("diary") @Valid DiaryCreateRequestDto request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        DiaryResponseDto response = diaryService.createDiary(user, request, images);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "일기 수정", description = "일기를 수정합니다.")
    @PutMapping(value = "/{diaryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DiaryResponseDto> updateDiary(
        @AuthenticationPrincipal User user,
        @PathVariable UUID diaryId,
        @RequestPart("diary") @Valid DiaryUpdateRequestDto request,
        @RequestPart(value = "images", required = false) List<MultipartFile> newImages) {

        // 기존 일기 가져오기
        Diary oldDiary = diaryService.getDiaryById(diaryId);

        // 유지할 이미지 목록
        List<String> keepUrls = (request.imageUrls() != null) ? request.imageUrls() : new ArrayList<>();

        // 기존 DB 이미지 중에서 삭제 대상 찾기
        List<String> toDelete = oldDiary.getImageUrls().stream()
            .filter(url -> !keepUrls.contains(url))
            .toList();

        if (!toDelete.isEmpty()) {
            s3Uploader.deleteFiles(toDelete);
        }

        // 새 이미지 업로드
        List<String> newUrls = new ArrayList<>();
        if (newImages != null && !newImages.isEmpty()) {
            newUrls = s3Uploader.uploadFiles(newImages, "diary/" + user.getUserId().toString());
        }

        // 최종 이미지 목록 = 유지할 것 + 새로 추가한 것
        List<String> finalUrls = new ArrayList<>(keepUrls);
        finalUrls.addAll(newUrls);
        DiaryUpdateRequestDto requestWithImages = new DiaryUpdateRequestDto(
            request.visitedAt(),
            finalUrls,
            request.content(),
            request.visibility()
        );

        DiaryResponseDto response = diaryService.updateDiary(user.getUserId(), diaryId, requestWithImages);
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

    @Operation(summary = "월별 일기 작성 여부 조회", description = "특정 유저의 일기 작성 여부를 일별로 조회합니다.")
    @GetMapping("/user/{userId}/writing-status")
    public ApiResponse<DiaryWritingStatusResponseDto> getUserMonthlyWritingStatus(
        @AuthenticationPrincipal User user,
        @PathVariable @Schema(description = "유저 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID userId,
        @RequestParam int year,
        @RequestParam int month) {
        DiaryWritingStatusResponseDto response = diaryService.getUserDiaryWritingStatus(user.getUserId(), userId, year, month);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "일별 일기 목록 조회", description = "특정 날짜에 작성된 일기 목록을 조회합니다.")
    @GetMapping("/daily")
    public ApiResponse<DailyDiaryListResponseDto> getDailyDiaries(
        @AuthenticationPrincipal User user,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(
            description = "조회할 날짜 (YYYY-MM-DD 형식)",
            example = "2025-08-10"
        )
        LocalDate date) {
        DailyDiaryListResponseDto response = diaryService.getDailyDiaries(user.getUserId(), date);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "특정 유저의 일기 목록 조회", description = "특정 유저가 작성한 일기 목록을 페이징하여 조회합니다. userId가 자신이면 public, follow, private 모든 일기 조회. 자신이 아니면 public 조회")
    @GetMapping("/user/{userId}")
    public ApiResponse<Page<DiaryResponseDto>> getUserPublicDiaries(
        @PathVariable @Schema(description = "유저 ID", example = "43d1a5a2-58dc-4786-8dba-27c62cae1943")
        UUID userId,
        @AuthenticationPrincipal User user,
        @ParameterObject Pageable pageable) {
        Page<DiaryResponseDto> response = diaryService.getUserDiaries(user.getUserId(), userId, pageable);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "이달의 인기글 조회", description = "현재 월의 인기글을 좋아요 수 기준으로 조회합니다.")
    @GetMapping("/popular")
    public ApiResponse<List<DiaryResponseDto>> getPopularDiariesOfMonth(
        @AuthenticationPrincipal User user
    ) {
        userRepository.findById(user.getUserId()).orElseThrow(UserNotFoundException::new);
        List<DiaryResponseDto> response = diaryService.getPopularDiariesOfMonth();
        return ApiResponse.ok(response);
    }

    @Operation(summary = "친구들 일기 모아보기", description = "팔로우한 친구들의 일기를 시간순으로 조회합니다. 베스트프렌드는 FOLLOWERS 일기도 볼 수 있습니다.")
    @GetMapping("/friends")
    public ApiResponse<Page<DiaryResponseDto>> getFriendsDiaries(
        @AuthenticationPrincipal User user,
        @ParameterObject Pageable pageable) {

        Page<DiaryResponseDto> response = diaryService.getFriendsDiaries(user.getUserId(), pageable);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "축제별 일기 작성 여부 확인", description = "사용자가 특정 축제에 대해 일기를 작성했는지 여부를 확인합니다.")
    @GetMapping("/written/{eventId}")
    public ApiResponse<Boolean> hasUserWrittenDiaryForEvent(
        @AuthenticationPrincipal User user,
        @PathVariable @Schema(description = "축제 ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID eventId) {
        boolean hasWritten = diaryService.getUserWrittenDiaryForEvent(user.getUserId(), eventId);
        return ApiResponse.ok(hasWritten);
    }
}
