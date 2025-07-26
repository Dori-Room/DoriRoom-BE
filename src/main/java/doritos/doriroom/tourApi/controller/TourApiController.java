package doritos.doriroom.tourApi.controller;

import doritos.doriroom.event.service.EventService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.tourApi.domain.Area;
import doritos.doriroom.tourApi.domain.Category;
import doritos.doriroom.tourApi.domain.Sigungu;
import doritos.doriroom.tourApi.service.AreaService;
import doritos.doriroom.tourApi.service.CategoryService;
import doritos.doriroom.tourApi.service.SigunguService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tour API 관련", description = "관광 정보 API - 지역 및 시군구 정보 조회, 데이터 초기화 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tour-api")
@Validated
public class TourApiController {
    private final AreaService areaService;
    private final SigunguService sigunguService;
    private final CategoryService categoryService;
    private final EventService eventService;

    @Operation(summary = "전체 지역 정보 조회", description = "지역코드와 지역명 매핑 정보를 목록으로 제공")
    @GetMapping("/area")
    public ApiResponse<List<Area>> getAllAreas() {
        List<Area> areas = areaService.getAllAreas();
        return ApiResponse.ok(areas);
    }

    @Operation(summary = "지역코드별 지역 정보 조회", description = "특정 지역코드에 해당하는 지역 정보를 조회")
    @GetMapping("/area/{areaCode}")
    public ApiResponse<Area> getAreaByCode(
        @Parameter(description = "지역코드 (1: 서울, 2: 인천)", example = "1", required = true)
        @PathVariable @NotNull Integer areaCode
    ) {
        Area area = areaService.getAreaByCode(areaCode);
        return ApiResponse.ok(area);
    }

    @Operation(summary = "지역별 시군구 목록 조회", description = "특정 지역의 시군구 정보를 목록으로 제공(예: 서울의 강남구, 강동구, 강북구 등)")
    @GetMapping("/sigungu/{areaCode}")
    public ApiResponse<List<Sigungu>> getSigunguByArea(
        @Parameter(description = "지역코드 (1: 서울, 2: 인천)", example = "1", required = true)
        @PathVariable @NotNull Integer areaCode
    ) {
        List<Sigungu> sigungus = sigunguService.getSigunguByAreaCode(areaCode);
        return ApiResponse.ok(sigungus);
    }

    @Operation(summary = "시군구 상세 정보 조회", description = "특정 지역의 특정 시군구 정보 조회 (예: 서울: 강남구)")
    @GetMapping("/sigungu/{areaCode}/{code}")
    public ApiResponse<Sigungu> getSigunguByCode(
        @Parameter(description = "지역코드 (1: 서울, 2: 인천)", example = "1", required = true)
        @PathVariable @NotNull Integer areaCode,
        @Parameter(description = "시군구 코드 (지역별로 다름, 예: 서울 강남구=1, 강동구=2)", example = "1", required = true)
        @PathVariable @NotNull Integer code
    ) {
        Sigungu sigungu = sigunguService.getSigunguByCode(areaCode, code);
        return ApiResponse.ok(sigungu);
    }

    @Operation(summary = "카테고리 조회", description = "축제(문화관광축제, 문화예술축제 등), 공연, 행사 코드를 조회")
    @GetMapping("/category")
    public ApiResponse<List<Map<String, Object>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<Map<String, Object>> response = categories.stream()
            .map(category -> {
                Map<String, Object> map = new HashMap<>();
                map.put("code", category.getCode());
                map.put("name", category.getName());
                return map;
            })
            .toList();
        return ApiResponse.ok(response);
    }

    @Operation(
        summary = "전체 축제 관련 정보 초기화 (관리자용)",
        description = """
        외부 API에서 최신 축제 정보, 지역 정보, 시군구 정보를 가져와서 데이터베이스에 저장합니다.
        
        **주의사항:**
        - 실행 시간: 약 10-15초 소요
        - 기존 데이터가 모두 삭제되고 새로운 데이터로 교체됩니다
        - 관리자만 사용해야 하는 기능입니다
        - 외부 API 호출이 많아 서버에 부하가 있을 수 있습니다
        
        **처리 과정:**
        1. 기존 축제 데이터 삭제 후 새로운 축제 데이터 저장
        2. 지역 정보 초기화 (17개 시도)
        3. 시군구 정보 초기화 (전국 모든 시군구)
        """
    )
    @PostMapping("/initialize")
    public ApiResponse<String> initializeAll() {
        eventService.getAllEvents();
        areaService.initializeAreas();
        sigunguService.initializeAllSigungu();
        return ApiResponse.ok("DB에 축제 관련 데이터 저장 완료");
    }

    @Operation(
        summary = "전체 축제 상세 정보 초기화 (관리자용)",
        description = """
            외부데이터에서 주회사, 주관사, 가격, 소개, 내용을 가져와 저장합니다.
            외부 API 호출이 많아 5분 정도 소요됩니다.

            TourAPI 하루 호출 최대 횟수가 5000회인데 이 트리거 한 번으로 약 3400회의 호출이 실행됩니다.
            **초기 데이터 생성 외에는 누르지 말아주세요.**
            """
    )
    @PostMapping("/initailize/details")
    public ApiResponse<String> initializeDetails(){
        eventService.updateAllEventDetails();
        return ApiResponse.ok("축제 상세정보 초기화 완료");
    }
} 