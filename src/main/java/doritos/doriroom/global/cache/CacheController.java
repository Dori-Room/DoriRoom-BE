package doritos.doriroom.global.cache;

import doritos.doriroom.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="캐시 관리", description = "Redis 캐시 관리 API (관리자용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/cache")
public class CacheController {
    private final RedisCacheService redisCacheService;

    @Operation(summary = "모든 캐시 삭제", description = "모든 Redis 캐시를 삭제합니다.")
    @DeleteMapping("/clear-all")
    public ApiResponse<String> clearAllCaches() {
        redisCacheService.clearAllCaches();
        return ApiResponse.ok("모든 캐시가 삭제되었습니다.");
    }

    @Operation(summary = "특정 캐시 삭제", description = "특정 캐시를 삭제합니다.")
    @DeleteMapping("/{cacheKey}")
    public ApiResponse<String> clearSpecificCache(@PathVariable String cacheKey) {
        redisCacheService.deleteCache(cacheKey);
        return ApiResponse.ok("캐시가 삭제되었습니다: " + cacheKey);
    }
}
