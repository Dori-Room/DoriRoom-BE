package doritos.doriroom.user.service;

import doritos.doriroom.user.domain.RoomLike;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.RoomLikeRepository;
import doritos.doriroom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RoomLikeService {
    private final RoomLikeRepository roomLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean setLikeStatus(User liker, UUID roomOwnerId, boolean isLiked) {
        try {
            // 방 주인 존재 여부 확인
            User roomOwner = userRepository.findByUserId(roomOwnerId)
                .orElseThrow(UserNotFoundException::new);

            // 자신의 방을 좋아요하려는 경우 예외 처리
            if (liker.getUserId().equals(roomOwnerId)) {
                throw new IllegalArgumentException("자신의 방은 좋아요할 수 없습니다.");
            }

            Optional<RoomLike> existingLike = roomLikeRepository.findByLikerIdAndRoomOwnerId(
                liker.getUserId(), roomOwnerId
            );

            if (isLiked) {
                if (existingLike.isPresent()) {
                    return true; // 이미 좋아요한 상태
                } else {
                    // 좋아요 추가
                    RoomLike roomLike = RoomLike.from(liker, roomOwner);
                    roomLikeRepository.save(roomLike);
                    roomOwner.incrementLikeCount();
                    userRepository.save(roomOwner);

                    return true;
                }
            } else {
                if (existingLike.isPresent()) {
                    // 좋아요 취소
                    roomLikeRepository.delete(existingLike.get());
                    roomOwner.decrementLikeCount();
                    userRepository.save(roomOwner);

                    return false;
                } else {
                    return false; // 이미 좋아요하지 않은 상태
                }
            }
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("방 좋아요 처리 중 오류가 발생했습니다.");
        }
    }

    public boolean isLiked(User liker, UUID roomOwnerId) {
        return roomLikeRepository.findByLikerIdAndRoomOwnerId(
            liker.getUserId(), roomOwnerId
        ).isPresent();
    }

    // 방 좋아요 수 조회
    public int getRoomLikeCount(UUID roomOwnerId) {
        User roomOwner = userRepository.findByUserId(roomOwnerId)
            .orElseThrow(UserNotFoundException::new);
        return roomOwner.getLikeCount();
    }
}