package doritos.doriroom.user.service;

import doritos.doriroom.user.domain.RoomLike;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.dto.response.RoomLikeResponseDto;
import doritos.doriroom.user.exception.RoomLikeException;
import doritos.doriroom.user.exception.SelfRoomLikeNotAllowedException;
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
    public RoomLikeResponseDto setLikeStatus(User liker, UUID roomOwnerId, boolean isLiked) {
        try {
            // 방 주인 존재 여부 확인
            User roomOwner = userRepository.findByUserId(roomOwnerId)
                .orElseThrow(UserNotFoundException::new);

            // 자신의 방을 좋아요하려는 경우 예외 처리
            if (liker.getUserId().equals(roomOwnerId)) {
                throw new SelfRoomLikeNotAllowedException();
            }

            Optional<RoomLike> existingLike = roomLikeRepository.findByLikerIdAndRoomOwnerId(
                liker.getUserId(), roomOwnerId
            );

            if (isLiked) {
                if (existingLike.isPresent()) {
                    int currentLikeCount = roomOwner.getLikeCount();
                    return RoomLikeResponseDto.builder()
                        .isLiked(true)
                        .likeCount(currentLikeCount)
                        .build();
                } else {
                    // 좋아요 추가
                    RoomLike roomLike = RoomLike.from(liker, roomOwner);
                    roomLikeRepository.save(roomLike);
                    roomOwner.incrementLikeCount();
                    userRepository.save(roomOwner);

                    int newLikeCount = roomOwner.getLikeCount();
                    return RoomLikeResponseDto.builder()
                        .isLiked(true)
                        .likeCount(newLikeCount)
                        .build();
                }
            } else {
                if (existingLike.isPresent()) {
                    // 좋아요 취소
                    roomLikeRepository.delete(existingLike.get());
                    roomOwner.decrementLikeCount();
                    userRepository.save(roomOwner);

                    int newLikeCount = roomOwner.getLikeCount();
                    return RoomLikeResponseDto.builder()
                        .isLiked(false)
                        .likeCount(newLikeCount)
                        .build();
                } else {
                    int currentLikeCount = roomOwner.getLikeCount();
                    return RoomLikeResponseDto.builder()
                        .isLiked(false)
                        .likeCount(currentLikeCount)
                        .build();
                }
            }
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RoomLikeException();
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