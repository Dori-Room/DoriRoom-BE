package doritos.doriroom.ranking.repository;

import doritos.doriroom.ranking.domain.ProfileVisit;
import doritos.doriroom.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileVisitRepository extends JpaRepository<ProfileVisit, UUID> {
    
    // 특정 방문자의 최근 방문 기록 조회 (최신순, 최대 20개)
    @Query("""
        SELECT pv FROM ProfileVisit pv
        WHERE pv.visitor.userId = :visitorId
        ORDER BY pv.visitedAt DESC
        """)
    List<ProfileVisit> findRecentVisitsByVisitorId(@Param("visitorId") UUID visitorId);
    
    // 특정 방문자가 특정 유저를 방문했는지 확인
    Optional<ProfileVisit> findByVisitorAndVisitedUser(User visitor, User visitedUser);
} 