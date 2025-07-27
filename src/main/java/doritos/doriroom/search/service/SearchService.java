package doritos.doriroom.search.service;

import doritos.doriroom.search.dto.SearchRankDto;
import doritos.doriroom.search.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    
    private final SearchRepository searchRepository;

    public void recordSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("빈 검색어는 기록하지 않습니다.");
            return;
        }
        
        String normalizedKeyword = normalizeKeyword(keyword);
        searchRepository.incrementSearchCount(normalizedKeyword);
    }
    
    public List<SearchRankDto> getPopularKeywordsWithRank() {
        return searchRepository.getTopKeywordsWithRankChange();
    }

    private String normalizeKeyword(String keyword) {
        return keyword.trim()
            .toLowerCase()
            .replaceAll("\\s+", " ");  // 연속 공백을 하나로
    }
} 