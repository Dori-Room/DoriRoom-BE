package doritos.doriroom.event.controller;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.request.EventItemFilterRequestDto;
import doritos.doriroom.event.dto.response.EventDetailResponseDto;
import doritos.doriroom.event.dto.response.EventResponseDto;
import doritos.doriroom.event.service.EventService;
import doritos.doriroom.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "축제 조회", description = "필터링 조건에 따라 축제 조회")
    @GetMapping("/filtered")
    public ApiResponse<Page<EventResponseDto>> getFilteredEvents(
        @ParameterObject EventItemFilterRequestDto request,
        @ParameterObject Pageable pageable
    ){
        return ApiResponse.ok(eventService.getFilteredEvents(request, pageable));
    }

    @Operation(summary = "축제 상세 정보 조회", description = "축제 상세 정보 조회")
    @GetMapping("/{eventId}")
    public ApiResponse<EventDetailResponseDto> getEventDetail(
        @Parameter(description = "축제 ID(eventId)", example = "01e0b423-af1b-44b3-a041-cf7753d72c53", required = true)
        @PathVariable UUID eventId
    ){
        return ApiResponse.ok(eventService.getEventDetail(eventId));
    }
}
