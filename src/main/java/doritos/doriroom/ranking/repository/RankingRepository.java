package doritos.doriroom.ranking.repository;

import doritos.doriroom.user.domain.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankingRepository extends JpaRepository<User, UUID> {
    
    // 좋아요 수 기준 상위 100명 조회
    List<User> findTop100ByOrderByLikeCountDesc();
} 