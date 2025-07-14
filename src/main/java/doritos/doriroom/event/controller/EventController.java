package doritos.doriroom.event.controller;

import doritos.doriroom.event.dto.response.EventApiItemDto;
import doritos.doriroom.event.service.EventService;
import doritos.doriroom.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="축제 정보 관련", description = "축제 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;

    @Operation(summary = "축제 전체 정보 조회")
    @GetMapping("/all")
    public ApiResponse<Page<EventApiItemDto>> getAllEvents(
        @Parameter(name = "startDate", description = "조회할 축제 시작 날짜", example = "20250101", required = true)
        @RequestParam(defaultValue = "20250101") String startDate,
        @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(eventService.getEvents(startDate, pageable));
    }
}
