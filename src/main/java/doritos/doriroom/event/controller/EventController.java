package doritos.doriroom.event.controller;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.response.EventResponseDto;
import doritos.doriroom.event.service.EventService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.tourApi.domain.AreaGroup;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    @Operation(summary = "도별 정보 조회", description = "도별 코드와 이름 정보 조회")
    @GetMapping("/area-groups")
    public ApiResponse<List<Map<String, Object>>> getAreaGroupEnumInfo() {
            List<Map<String, Object>> areaGroups = Stream.of(AreaGroup.values())
                .map(group -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", group.getCode());
                    map.put("name", group.getName());
                    return map;
                })
                .collect(Collectors.toList());
            return ApiResponse.ok(areaGroups);
    }

    @Operation(summary = "도별 축제 조회", description = "서울, 경기, 경상 별로 축제 정보 조회")
    @GetMapping("/area-group/{groupCode}")
    public ApiResponse<Page<EventResponseDto>> getAreaGroupEvents(
        @PathVariable Integer groupCode,
        @ParameterObject Pageable pageable
    ){
        return ApiResponse.ok(eventService.getEventsByAreaGroup(groupCode, pageable));
    }

    @Operation(summary = "시군구별 축제 조회", description = "지역코드와 시군구코드 리스트로 축제 조회")
    @GetMapping("/{areaCode}/sigungu")
    public ApiResponse<Page<EventResponseDto>> getEventsByAreaAndSigungu(
        @Parameter(description = "지역코드 (1: 서울, 2: 인천)", example = "1", required = true)
        @PathVariable @NotNull Integer areaCode,
        @Parameter(description = "시군구코드 리스트 ", example = "[1, 2]", required = true)
        @RequestParam @NotNull List<Integer> sigunguCodes,
        @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(eventService.getEventsByAreaAndSigungu(areaCode, sigunguCodes, pageable));
    }
}
