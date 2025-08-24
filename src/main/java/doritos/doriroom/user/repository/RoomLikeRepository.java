package doritos.doriroom.user.repository;

import doritos.doriroom.user.domain.RoomLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomLikeRepository extends JpaRepository<RoomLike, UUID> {
    // 특정 유저가 특정 방을 좋아요했는지 확인
    @Query("SELECT rl FROM RoomLike rl WHERE rl.liker.userId = :likerId AND rl.roomOwner.userId = :roomOwnerId")
    Optional<RoomLike> findByLikerIdAndRoomOwnerId(
        @Param("likerId") UUID likerId,
        @Param("roomOwnerId") UUID roomOwnerId
    );
}
