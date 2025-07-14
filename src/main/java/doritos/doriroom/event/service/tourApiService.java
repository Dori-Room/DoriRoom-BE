package doritos.doriroom.event.service;

import doritos.doriroom.event.dto.response.EventApiItemDto;
import doritos.doriroom.event.dto.response.EventApiResponseDto;
import doritos.doriroom.event.exception.ExternalApiException;
import java.util.ArrayList;
import java.util.List;
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
public class tourApiService {
    private final WebClient webClient;

    @Value("${tour-api.event.url}")
    private String eventPath;

    @Value("${tour-api.event.key}")
    private String serviceKey;

    @Transactional
    public List<EventApiItemDto> fetchEvents() {
        int pageSize = 1000;
        int currentPage = 1;

        // 1. 첫 페이지로 totalCount 확인
        EventApiResponseDto firstResponse = fetchPage(currentPage, pageSize);
        long totalCount = firstResponse.getResponse().getBody().getTotalCount();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        List<EventApiItemDto> result = new ArrayList<>(firstResponse.getResponse().getBody().getItems().getItem());

        // 2. 나머지 페이지 반복 호출
        for (int page = 2; page <= totalPages; page++) {
            List<EventApiItemDto> items = fetchPage(page, pageSize)
                .getResponse().getBody().getItems().getItem();
            result.addAll(items);
        }

        return result;
    }

    private EventApiResponseDto fetchPage(int pageNo, int numOfRows) {
        try{
            EventApiResponseDto response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(eventPath)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "DoriRoom")
                    .queryParam("eventStartDate", "20160101") //날짜 변경하기
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("_type", "json")
                    .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, res ->
                    res.bodyToMono(String.class).map(body -> new ExternalApiException("4xx Error: " + body))
                )
                .onStatus(HttpStatusCode::is5xxServerError, res ->
                    res.bodyToMono(String.class).map(body -> new ExternalApiException("5xx Error: " + body))
                )
                .bodyToMono(EventApiResponseDto.class)
                .block();

            if (response == null || response.getResponse() == null
                || response.getResponse().getBody() == null
                || response.getResponse().getBody().getItems() == null) {
                return null;
            }

            return response;
        } catch (Exception e){
            throw new ExternalApiException("TOUR API 호출 실패" + e.getMessage());
        }
    }
}
