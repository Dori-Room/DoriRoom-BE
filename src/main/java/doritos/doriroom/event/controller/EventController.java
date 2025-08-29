package doritos.doriroom.event.controller;

import doritos.doriroom.diary.service.DiaryService;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.request.EventItemFilterRequestDto;
import doritos.doriroom.event.dto.response.EventDetailResponseDto;
import doritos.doriroom.event.dto.response.EventDiaryResponseDto;
import doritos.doriroom.event.dto.response.EventResponseDto;
import doritos.doriroom.event.service.EventService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.search.service.SearchService;
import doritos.doriroom.tourApi.domain.AreaGroup;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name="축제 정보 관련", description = "축제 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;
    private final SearchService searchService;
    private final DiaryService diaryService;

    @Operation(summary = "따끈따끈 신규 축제 조회")
    @GetMapping("/upcoming")
    public ApiResponse<List<EventResponseDto>> getUpcomingEvents(){
        List<Event> events = eventService.getUpcomingEvents();
        List<EventResponseDto> response = events.stream()
            .map(EventResponseDto::from)
            .toList();

        return ApiResponse.ok(response);
    }

    @Operation(summary = "마감 임박 축제 조회")
    @GetMapping("/ending-soon")
    public ApiResponse<List<EventResponseDto>> getEndingSoonEvents(){
        List<Event> events = eventService.getEndingSoonEvents();

        List<EventResponseDto> response = events.stream()
            .map(EventResponseDto::from)
            .toList();

        return ApiResponse.ok(response);
    }

    @Operation(summary = "지금 뜨는 축제 조회")
    @GetMapping("/popular")
    public ApiResponse<List<EventResponseDto>> getPopularEvents(){
        List<Event> events = eventService.getPopularEvents();

        List<EventResponseDto> response = events.stream()
            .map(EventResponseDto::from)
            .toList();

        return ApiResponse.ok(response);
    }

    @Operation(summary = "축제 조회", description = "필터링 조건에 따라 축제 조회")
    @PostMapping("/filtered")
    public ApiResponse<Page<EventResponseDto>> getFilteredEvents(
        @Valid @RequestBody EventItemFilterRequestDto request,
        @ParameterObject Pageable pageable
    ){
        //인기검색어 카운트+1
        if(request.keyword() != null && !request.keyword().trim().isEmpty()){
            searchService.recordSearch(request.keyword());
        }

        return ApiResponse.ok(eventService.getFilteredEvents(request, pageable));
    }

    @Operation(summary = "축제 상세 정보 조회", description = "축제 상세 정보 조회")
    @GetMapping("/detail/{eventId}")
    public ApiResponse<EventDetailResponseDto> getEventDetail(
        @Parameter(description = "축제ID", example = "39eccbae-769f-48cc-b9e8-132a33547280", required = true)
        @PathVariable("eventId") UUID eventId
    ){
        return ApiResponse.ok(eventService.getEventDetail(eventId));
    }

    @Operation(summary = "도별 축제 조회", description = "8도 축제 정보 조회")
    @GetMapping("/area/{areaGroupCode}")
    public ApiResponse<Page<EventResponseDto>> getEventsByAreaGroup(
        @Parameter(description = "지역 그룹 코드(1: 서울, 2:경기도)", example = "1", required = true)
        @PathVariable int areaGroupCode,
        @ParameterObject Pageable pageable
    ){
        AreaGroup areaGroup = AreaGroup.fromCode(areaGroupCode);

        Page<EventResponseDto> events = eventService.getEventsByAreaGroup(areaGroup, pageable);
        return ApiResponse.ok(events);
    }

    @Operation(summary = "축제별 일기 조회", description = "축제별 일기 리스트 조회")
    @GetMapping("/{eventId}/diaries")
    public ApiResponse<EventDiaryResponseDto> getEventDiaries(
        @PathVariable @Schema(description = "축제 ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID eventId,
        @ParameterObject Pageable pageable) {
        EventDiaryResponseDto response = diaryService.getDiariesByEventId(eventId, pageable);
        return ApiResponse.ok(response);
    }
}
