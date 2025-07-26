package doritos.doriroom.tourApi.service;

import doritos.doriroom.tourApi.domain.Sigungu;
import doritos.doriroom.tourApi.domain.SigunguId;
import doritos.doriroom.tourApi.dto.response.SigunguApiItemDto;
import doritos.doriroom.tourApi.dto.response.SigunguApiResponseDto;
import doritos.doriroom.tourApi.exception.ExternalApiException;
import doritos.doriroom.tourApi.exception.SigunguNotFoundException;
import doritos.doriroom.tourApi.repository.SigunguRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SigunguService {
    private final WebClient webClient;
    private final SigunguRepository sigunguRepository;

    @Value("${tour-api.sigungu.url}")
    private String sigunguPath;

    @Value("${tour-api.sigungu.key}")
    private String serviceKey;

    /**
     * 특정 지역의 시군구 정보를 API에서 가져와서 DB에 저장
     */
    @Transactional
    public void initializeSigunguByArea(Integer areaCode, String areaName) {
        log.info("지역 {} ({})의 시군구 정보 초기화 시작", areaName, areaCode);
        
        try {
            SigunguApiResponseDto response = webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(sigunguPath)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "DoriRoom")
                        .queryParam("areaCode", areaCode.toString())
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("_type", "json");
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, res ->
                    res.bodyToMono(String.class).map(body -> new ExternalApiException("4xx Error: " + body))
                )
                .onStatus(HttpStatusCode::is5xxServerError, res ->
                    res.bodyToMono(String.class).map(body -> new ExternalApiException("5xx Error: " + body))
                )
                .bodyToMono(SigunguApiResponseDto.class)
                .block();

            if (response == null || response.getResponse() == null || 
                response.getResponse().getBody() == null || 
                response.getResponse().getBody().getItems() == null) {
                log.warn("지역 {}의 시군구 정보가 없습니다.", areaName);
                return;
            }

            List<SigunguApiItemDto> items = response.getResponse().getBody().getItems().getItem();
            
            List<Sigungu> sigungus = items.stream()
                .map(item -> Sigungu.builder()
                    .code(Integer.parseInt(item.getCode()))
                    .name(item.getName())
                    .areaCode(areaCode)
                    .areaName(areaName)
                    .build())
                .toList();

            sigunguRepository.saveAll(sigungus);
            log.info("지역 {}의 시군구 정보 {}개 저장 완료", areaName, sigungus.size());
            
        } catch (Exception e) {
            log.error("지역 {}의 시군구 정보 초기화 실패: {}", areaName, e.getMessage());
            throw new ExternalApiException("시군구 API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * 모든 지역의 시군구 정보 초기화
     */
    @Transactional
    public void initializeAllSigungu() {
        sigunguRepository.deleteAll();
        log.info("기존 시군구 정보 삭제 완료");

        // 각 지역별로 시군구 정보 가져오기
        Object[][] areas = {
            {1, "서울"}, {2, "인천"}, {3, "대전"}, {4, "대구"}, {5, "광주"},
            {6, "부산"}, {7, "울산"}, {8, "세종"}, {31, "경기"}, {32, "강원"},
            {33, "충북"}, {34, "충남"}, {35, "경북"}, {36, "경남"}, {37, "전북"},
            {38, "전남"}, {39, "제주"}
        };

        for (Object[] area : areas) {
            try {
                initializeSigunguByArea((Integer) area[0], (String) area[1]);
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("지역 {} 시군구 초기화 실패: {}", area[1], e.getMessage());
            }
        }
        
        log.info("전체 시군구 정보 초기화 완료");
    }

    /**
     * 특정 지역의 시군구 목록 조회
     */
    public List<Sigungu> getSigunguByAreaCode(Integer areaCode) {
        return sigunguRepository.findByAreaCode(areaCode);
    }

    /**
     * 시군구 코드로 시군구 정보 조회
     */
    public Sigungu getSigunguByCode(Integer areaCode, Integer code) {
        SigunguId sigunguId = new SigunguId(areaCode, code);
        return sigunguRepository.findById(sigunguId)
            .orElseThrow(SigunguNotFoundException::new);
    }
} 