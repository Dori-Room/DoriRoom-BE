package doritos.doriroom.event.service;

import doritos.doriroom.event.dto.response.EventApiItemDto;
import doritos.doriroom.event.dto.response.EventApiResponseDto;
import doritos.doriroom.event.exception.ExternalApiException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TourApiService {
    private final WebClient webClient;

    @Value("${tour-api.event.url}")
    private String eventPath;

    @Value("${tour-api.event.key}")
    private String serviceKey;

    /**
     * 전체 축제 데이터 가져오는 메서드
     */
    @Transactional
    public List<EventApiItemDto> fetchAllEvents() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("eventStartDate", "20160101");
        return fetchAllPages(queryParams);
    }

    /**
     * modifiedTime 또는 eventStartDate가 오늘인 데이터만 가져오는 메서드
     */
    @Transactional
    public List<EventApiItemDto> fetchTodayEvents() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // eventStartDate만 넣고 호출
        Map<String, String> startDateParams = new HashMap<>();
        startDateParams.put("eventStartDate", today);
        List<EventApiItemDto> fromStartDate = fetchAllPages(startDateParams);

        // modifiedtime만 사용하는 호출
        Map<String, String> modifiedParams = new HashMap<>();
        modifiedParams.put("eventStartDate", "20160101");
        modifiedParams.put("modifiedtime", "20250717");
        List<EventApiItemDto> fromModifiedTime = fetchAllPages(modifiedParams);

        // 3. contentId 기준으로 중복 제거
        Map<String, EventApiItemDto> merged = new LinkedHashMap<>();

        for (EventApiItemDto dto : fromStartDate) {
            if (dto.getContentid() != null && !dto.getContentid().isBlank()) {
                merged.put(dto.getContentid(), dto);
            }
        }

        for (EventApiItemDto dto : fromModifiedTime) {
            if (dto.getContentid() != null && !dto.getContentid().isBlank()) {
                merged.put(dto.getContentid(), dto); // 동일 contentId면 덮어씀
            }
        }

        log.info("오늘 기준으로 가져온 이벤트: {}건 (startDate 기반: {}, modifiedTime 기반: {})",
            merged.size(), fromStartDate.size(), fromModifiedTime.size());

        return new ArrayList<>(merged.values());
    }

    /**
     * 페이징 전체 반복 로직
     */
    private List<EventApiItemDto> fetchAllPages(Map<String, String> extraParams) {
        int pageSize = 1000;
        int currentPage = 1;

        EventApiResponseDto firstResponse = fetchPage(currentPage, pageSize, extraParams);
        if (firstResponse == null || firstResponse.getResponse() == null || firstResponse.getResponse().getBody() == null)
            return Collections.emptyList();

        long totalCount = firstResponse.getResponse().getBody().getTotalCount();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        log.info("총 축제 수: {}, 총 페이지 수: {}", totalCount, totalPages);

        List<EventApiItemDto> result = new ArrayList<>(firstResponse.getResponse().getBody().getItems().getItem());

        for (int page = 2; page <= totalPages; page++) {
            EventApiResponseDto response = fetchPage(page, pageSize, extraParams);
            if (response != null &&
                response.getResponse() != null &&
                response.getResponse().getBody() != null &&
                response.getResponse().getBody().getItems() != null) {
                result.addAll(response.getResponse().getBody().getItems().getItem());
            }
        }

        return result;
    }

    /**
     * 실제 단일 페이지 호출 (API 요청)
     */
    private EventApiResponseDto fetchPage(int pageNo, int numOfRows, Map<String, String> extraParams) {
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(eventPath)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "DoriRoom")
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("_type", "json");

                    extraParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                });

            return request
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, res ->
                    res.bodyToMono(String.class).map(body -> new ExternalApiException("4xx Error: " + body))
                )
                .onStatus(HttpStatusCode::is5xxServerError, res ->
                    res.bodyToMono(String.class).map(body -> new ExternalApiException("5xx Error: " + body))
                )
                .bodyToMono(EventApiResponseDto.class)
                .block();
        } catch (Exception e) {
            throw new ExternalApiException("TOUR API 호출 실패: " + e.getMessage());
        }
    }
}
