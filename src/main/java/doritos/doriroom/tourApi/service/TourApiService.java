package doritos.doriroom.tourApi.service;

import doritos.doriroom.tourApi.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TourApiService {
    private final TourApiClient tourApiClient;

    @Value("${tour-api.event.url}")
    private String eventPath;

    @Value("${tour-api.event-detail-intro.url}")
    private String eventDetailIntroPath;

    @Value("${tour-api.event-detail-info.url}")
    private String eventDetailInfoPath;

    //전체 축제 데이터 가져오기
    @Transactional
    public List<TourApiItemDto> fetchAllEvents() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("eventStartDate", "20160101");
        return fetchAllPages(queryParams);
    }

     //오늘 업데이트된 축제 데이터 가져오기
    @Transactional
    public List<TourApiItemDto> fetchTodayEvents() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // eventStartDate만 넣고 호출
        Map<String, String> startDateParams = new HashMap<>();
        startDateParams.put("eventStartDate", today);
        List<TourApiItemDto> fromStartDate = fetchAllPages(startDateParams);

        // modifiedtime만 사용하는 호출
        Map<String, String> modifiedParams = new HashMap<>();
        modifiedParams.put("eventStartDate", "20160101");
        modifiedParams.put("modifiedtime", "20250717");
        List<TourApiItemDto> fromModifiedTime = fetchAllPages(modifiedParams);

        // 3. contentId 기준으로 중복 제거
        Map<String, TourApiItemDto> merged = new LinkedHashMap<>();

        for (TourApiItemDto dto : fromStartDate) {
            if (dto.getContentid() != null && !dto.getContentid().isBlank()) {
                merged.put(dto.getContentid(), dto);
            }
        }

        for (TourApiItemDto dto : fromModifiedTime) {
            if (dto.getContentid() != null && !dto.getContentid().isBlank()) {
                merged.put(dto.getContentid(), dto); // 동일 contentId면 덮어씀
            }
        }

        log.info("오늘 기준으로 가져온 이벤트: {}건 (startDate 기반: {}, modifiedTime 기반: {})",
            merged.size(), fromStartDate.size(), fromModifiedTime.size());

        return new ArrayList<>(merged.values());
    }

    //특정 축제의 상세정보(detailIntro2) 가져오기 (주관사, 주최사, 가격)
    public TourApiDetailIntroDto fetchEventDetailIntro(int contentId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("contentId", String.valueOf(contentId));
        queryParams.put("contentTypeId", "15");

        TourApiDetailIntroResponseDto response = tourApiClient.callApi(eventDetailIntroPath, queryParams, TourApiDetailIntroResponseDto.class);
        
        if (response == null || response.getResponse() == null ||
            response.getResponse().getBody() == null ||
            response.getResponse().getBody().getItems() == null ||
            response.getResponse().getBody().getItems().getItem().isEmpty()) {
            log.warn("축제 상세정보가 없습니다. contentId: {}", contentId);
            return null;
        }

        return response.getResponse().getBody().getItems().getItem().get(0);
    }

     //특정 축제의 상세정보(detailInfo2) 가져오기 (행사소개, 행사내용)
    public List<TourApiDetailInfoDto> fetchEventDetailInfo(int contentId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("contentId", String.valueOf(contentId));
        queryParams.put("contentTypeId", "15");

        TourApiDetailInfoResponseDto response = tourApiClient.callApi(eventDetailInfoPath, queryParams, TourApiDetailInfoResponseDto.class);
        
        if (response == null || response.getResponse() == null ||
            response.getResponse().getBody() == null ||
            response.getResponse().getBody().getItems() == null ||
            response.getResponse().getBody().getItems().getItem().isEmpty()) {
            log.warn("축제 상세정보(detailInfo2)가 없습니다. contentId: {}", contentId);
            return new ArrayList<>();
        }

        List<TourApiDetailInfoDto> items = response.getResponse().getBody().getItems().getItem();
        log.info("축제 상세정보 조회 완료. contentId: {}, 조회된 항목 수: {}", contentId, items.size());
        
        return items;
    }

    // 페이징 전체 반복 로직
    private List<TourApiItemDto> fetchAllPages(Map<String, String> extraParams) {
        int pageSize = 1000;
        int currentPage = 1;

        Map<String, String> firstPageParams = new HashMap<>(extraParams);
        firstPageParams.put("pageNo", String.valueOf(currentPage));
        firstPageParams.put("numOfRows", String.valueOf(pageSize));

        TourApiResponseDto firstResponse = tourApiClient.callApi(eventPath, firstPageParams, TourApiResponseDto.class);
        if (firstResponse == null || firstResponse.getResponse() == null || firstResponse.getResponse().getBody() == null)
            return Collections.emptyList();

        long totalCount = firstResponse.getResponse().getBody().getTotalCount();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        log.info("총 축제 수: {}, 총 페이지 수: {}", totalCount, totalPages);

        List<TourApiItemDto> result = new ArrayList<>(firstResponse.getResponse().getBody().getItems().getItem());

        for (int page = 2; page <= totalPages; page++) {
            Map<String, String> pageParams = new HashMap<>(extraParams);
            pageParams.put("pageNo", String.valueOf(page));
            pageParams.put("numOfRows", String.valueOf(pageSize));

            TourApiResponseDto response = tourApiClient.callApi(eventPath, pageParams, TourApiResponseDto.class);
            if (response != null &&
                response.getResponse() != null &&
                response.getResponse().getBody() != null &&
                response.getResponse().getBody().getItems() != null) {
                result.addAll(response.getResponse().getBody().getItems().getItem());
            }
        }

        return result;
    }
}
