package doritos.doriroom.diary.service;

import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.diary.dto.request.DiaryCreateRequestDto;
import doritos.doriroom.diary.dto.request.DiaryUpdateRequestDto;
import doritos.doriroom.diary.dto.response.DiaryResponseDto;
import doritos.doriroom.diary.exception.DiaryAuthorizationException;
import doritos.doriroom.diary.exception.DiaryNotFoundException;
import doritos.doriroom.diary.repository.DiaryRepository;
import doritos.doriroom.user.exception.UsernameNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    @Transactional
    public DiaryResponseDto createDiary(UUID userId, DiaryCreateRequestDto request){
        userRepository.findById(userId).orElseThrow(UsernameNotFoundException::new);

        Diary diary = Diary.from(userId, request);
        return DiaryResponseDto.from(diaryRepository.save(diary));
    }

    @Transactional
    public DiaryResponseDto updateDiary(UUID userId, UUID diaryId, DiaryUpdateRequestDto request) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);

        if (!diary.getUserId().equals(userId)) {
            throw new DiaryAuthorizationException();
        }

        // 일기 정보 업데이트
        diary.updateDiary(request);

        Diary updatedDiary = diaryRepository.save(diary);

        return DiaryResponseDto.from(updatedDiary);
    }

    @Transactional
    public void deleteDiary(UUID userId, UUID diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);

        if(!diary.getUserId().equals(userId)) {
            throw new DiaryAuthorizationException();
        }

        diaryRepository.delete(diary);
    }

}
