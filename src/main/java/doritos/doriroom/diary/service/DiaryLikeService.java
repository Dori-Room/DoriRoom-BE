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
    private final NotificationService notificationService;

    @Transactional
    public boolean setLikeStatus(User user, UUID diaryId, boolean isLiked) {
        try {
            Object[] list = diaryRepository.findDiaryWithUser(diaryId).orElseThrow(DiaryNotFoundException::new);
            Diary diary = (Diary) list[0];
            User diaryOwner = (User) list[1];

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

                    // 일기 주인에게 좋아요 알림 발송
                    notificationService.sendNotification(diaryOwner, NotificationType.DIARY_LIKE, user.getNickname(), diary.getDiaryId().toString());

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
            throw e;
        } catch (Exception e) {
            throw new DiaryLikeException();
        }
    }

    public boolean isLiked(User user, UUID diaryId) {
        return diaryLikeRepository.findByUserIdAndDiaryId(user.getUserId(), diaryId).isPresent();
    }
}
