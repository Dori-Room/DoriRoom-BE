package doritos.doriroom.ranking.service;

import doritos.doriroom.ranking.domain.ProfileVisit;
import doritos.doriroom.ranking.dto.response.RecentVisitResponseDto;
import doritos.doriroom.ranking.repository.ProfileVisitRepository;
import doritos.doriroom.user.domain.User;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProfileVisitService {
    private final ProfileVisitRepository profileVisitRepository;

    // 최근 방문한 프로필 조회
    public List<RecentVisitResponseDto> getRecentVisits(User currentUser) {
        List<ProfileVisit> recentVisits = profileVisitRepository.findRecentVisitsByVisitorId(currentUser.getUserId());

        if (recentVisits.isEmpty()) {
            return List.of();
        }

        // 응답 DTO 변환
        List<RecentVisitResponseDto> recentVisitList = new ArrayList<>();

        for (ProfileVisit visit : recentVisits) {
            User visitedUser = visit.getVisitedUser();

            recentVisitList.add(RecentVisitResponseDto.builder()
                .userId(visitedUser.getUserId())
                .nickname(visitedUser.getNickname())
                .profileImageUrl(visitedUser.getProfileImageUrl())
                .visitedAt(visit.getVisitedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build());
        }
        return recentVisitList;
    }

    // 프로필 방문 기록 추가
    @Transactional
    public void addProfileVisit(User visitor, User visitedUser) {
        // 자신의 프로필을 방문하는 경우는 기록하지 않음
        if (visitor.getUserId().equals(visitedUser.getUserId())) {
            return;
        }

        // 이미 방문 기록이 있는지 확인
        if (profileVisitRepository.existsByVisitorAndVisitedUser(visitor, visitedUser)) {
            // 기존 기록 삭제 후 새로 추가 (최신 방문 시간으로 업데이트)
            profileVisitRepository.deleteByVisitorAndVisitedUser(visitor, visitedUser);
        }

        // 새로운 방문 기록 추가
        ProfileVisit profileVisit = ProfileVisit.builder()
            .visitor(visitor)
            .visitedUser(visitedUser)
            .build();

        profileVisitRepository.save(profileVisit);
    }
}
