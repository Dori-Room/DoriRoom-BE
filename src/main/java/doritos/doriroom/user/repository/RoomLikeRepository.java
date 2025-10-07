package doritos.doriroom.user.repository;

import doritos.doriroom.user.domain.RoomLike;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import doritos.doriroom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    List<RoomLike> findByLiker(User liker); // 내가 누른 좋아요 찾기
    @Modifying
    @Query("DELETE FROM RoomLike rl WHERE rl.roomOwner = :user")
    void deleteAllByRoomOwner(@Param("user") User user); // 내 방에 달린 좋아요 삭제

}
