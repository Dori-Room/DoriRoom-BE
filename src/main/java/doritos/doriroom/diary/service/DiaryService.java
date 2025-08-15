package doritos.doriroom.diary.service;

import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.diary.dto.request.DiaryCreateRequestDto;
import doritos.doriroom.diary.dto.response.DiaryResponseDto;
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

}
