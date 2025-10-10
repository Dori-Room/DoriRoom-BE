package doritos.doriroom.diary.service;

import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.diary.domain.DiaryLike;
import doritos.doriroom.diary.exception.DiaryLikeException;
import doritos.doriroom.diary.exception.DiaryNotFoundException;
import doritos.doriroom.diary.repository.DiaryLikeRepository;
import doritos.doriroom.diary.repository.DiaryRepository;
import doritos.doriroom.notification.domain.NotificationType;
import doritos.doriroom.notification.service.NotificationService;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryLikeService {
    private final DiaryLikeRepository diaryLikeRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public boolean setLikeStatus(User user, UUID diaryId, boolean isLiked) {
        try {
            // 일기 조회
            Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(DiaryNotFoundException::new);
            
            // 일기 작성자 조회
            User diaryOwner = userRepository.findByUserId(diary.getUserId())
                .orElseThrow(UserNotFoundException::new);

            Optional<DiaryLike> existingLike = diaryLikeRepository.findByUserIdAndDiaryId(user.getUserId(), diaryId);

            if (isLiked) {
                if (existingLike.isPresent()) {
                    return true;
                } else {
                    DiaryLike like = DiaryLike.builder()
                        .user(user)
                        .diary(diary)
                        .build();

                    diaryLikeRepository.save(like);
                    diary.incrementLikes();
                    diaryRepository.save(diary);

                    try {
                        notificationService.sendNotification(
                                diaryOwner,
                                NotificationType.DIARY_LIKE,
                                user.getNickname(),
                                diary.getDiaryId().toString()
                        );
                    } catch (Exception notifyEx) {
                        log.warn("알림 전송 실패: {}", notifyEx.getMessage());
                    }

                    return true;
                }
            } else {
                if (existingLike.isPresent()) {
                    diaryLikeRepository.delete(existingLike.get());
                    diary.decrementLikes();
                    diaryRepository.save(diary);
                    return false;
                } else {
                    return false;
                }
            }
        } catch (DiaryNotFoundException e) {
            log.error("일기를 찾을 수 없습니다 - diaryId: {}", diaryId);
            throw e;
        } catch (UserNotFoundException e) {
            log.error("일기 작성자를 찾을 수 없습니다 - diaryId: {}", diaryId);
            throw e;
        } catch (Exception e) {
            log.error("일기 좋아요 처리 중 오류 발생 - userId: {}, diaryId: {}, isLiked: {}, error: {}", 
                user.getUserId(), diaryId, isLiked, e.getMessage(), e);
            throw new DiaryLikeException();
        }
    }

    public boolean isLiked(User user, UUID diaryId) {
        return diaryLikeRepository.findByUserIdAndDiaryId(user.getUserId(), diaryId).isPresent();
    }
}
