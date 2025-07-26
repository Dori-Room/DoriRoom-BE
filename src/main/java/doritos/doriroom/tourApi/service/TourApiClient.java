package doritos.doriroom.tourApi.service;

import doritos.doriroom.tourApi.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourApiClient {
    private final WebClient webClient;

    @Value("${tour-api.event.key}")
    private String serviceKey;

    //공통 API 호출 메소드
    public <T> T callApi(String path, Map<String, String> queryParams, Class<T> responseType) {
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path)
                        .queryParam("MobileOS", "APP")
                        .queryParam("MobileApp", "DoriRoom")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("_type", "json");

                    queryParams.forEach(uriBuilder::queryParam);
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
                .bodyToMono(responseType)
                .block();
        } catch (Exception e) {
            throw new ExternalApiException("TOUR API 호출 실패: " + e.getMessage());
        }
    }
} 