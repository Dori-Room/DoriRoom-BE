package doritos.doriroom.event.service;

import doritos.doriroom.event.dto.response.EventApiItemDto;
import doritos.doriroom.event.dto.response.EventApiResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
    private final WebClient webClient;

    @Value("${tour-api.event.url}")
    private String eventUrl;

    @Value("${tour-api.event.key}")
    private String serviceKey;

    public Page<EventApiItemDto> getEvents(String startDate, Pageable pageable) {
        String path = eventUrl.replace("https://apis.data.go.kr", "");

        int pageNo = pageable.getPageNumber() + 1;
        int numOfRows = pageable.getPageSize();

        EventApiResponseDto response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(path)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "DoriRoom")
                .queryParam("eventStartDate", startDate)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("serviceKey", serviceKey)
                .queryParam("_type", "json")
                .build()
            )
            .retrieve()
            .bodyToMono(EventApiResponseDto.class)
            .block();

        if(response == null || response.getResponse() == null
            || response.getResponse().getBody() == null
            || response.getResponse().getBody().getItems() == null) {
            return Page.empty(pageable);
        }

        List<EventApiItemDto> items = response.getResponse().getBody().getItems().getItem();
        long totalCount = response.getResponse().getBody().getTotalCount();

        return new PageImpl<>(items, pageable, totalCount);
    }
}
