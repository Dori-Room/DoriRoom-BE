package doritos.doriroom.diary.service;

import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.diary.domain.DiaryLike;
import doritos.doriroom.diary.exception.DiaryLikeException;
import doritos.doriroom.diary.exception.DiaryNotFoundException;
import doritos.doriroom.diary.repository.DiaryLikeRepository;
import doritos.doriroom.diary.repository.DiaryRepository;
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

    @Transactional
    public boolean setLikeStatus(User user, UUID diaryId, boolean isLiked) {
        try {
            Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);

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
