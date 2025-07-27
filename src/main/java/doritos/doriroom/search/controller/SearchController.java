package doritos.doriroom.search.controller;

import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.search.dto.SearchRankDto;
import doritos.doriroom.search.service.SearchService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="검색 관련", description = "오늘의 실시간 검색어 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    
    private final SearchService searchService;
    
    @Operation(summary = "오늘의 인기 검색어 조회", description = "오늘의 인기 검색어를 순위와 함께 조회")
    @GetMapping("/popular")
    public ApiResponse<List<SearchRankDto>> getPopularKeywords(
    ) {
        List<SearchRankDto> keywords = searchService.getPopularKeywordsWithRank();
        return ApiResponse.ok(keywords);
    }
} 