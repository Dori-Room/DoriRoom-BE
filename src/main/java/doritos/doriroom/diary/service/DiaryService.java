package doritos.doriroom.diary.service;

import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.diary.dto.request.DiaryCreateRequestDto;
import doritos.doriroom.diary.dto.request.DiaryUpdateRequestDto;
import doritos.doriroom.diary.dto.response.DailyDiaryListResponseDto;
import doritos.doriroom.diary.dto.response.DiaryDetailResponseDto;
import doritos.doriroom.diary.dto.response.DiaryResponseDto;
import doritos.doriroom.diary.dto.response.DiaryWritingStatusResponseDto;
import doritos.doriroom.diary.exception.DiaryAuthorizationException;
import doritos.doriroom.diary.exception.DiaryNotFoundException;
import doritos.doriroom.diary.repository.DiaryRepository;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.response.EventDiaryResponseDto;
import doritos.doriroom.event.exception.EventNotFoundException;
import doritos.doriroom.event.repository.EventRepository;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UsernameNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

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

    public DiaryDetailResponseDto getDiaryDetail(UUID diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
            .orElseThrow(DiaryNotFoundException::new);

        User user = userRepository.findById(diary.getUserId())
            .orElseThrow(UsernameNotFoundException::new);

        Event event = eventRepository.findById(diary.getEventId())
            .orElseThrow(EventNotFoundException::new);

        return DiaryDetailResponseDto.from(diary, user, event);
    }

    //월별 일기 작성 여부 조회
    public DiaryWritingStatusResponseDto getDiaryWritingStatus(UUID userId, int year, int month) {
        // 해당 월의 시작일과 종료일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 해당 월에 작성된 일기 조회
        List<Diary> diaries = diaryRepository.findByUserIdAndVisitedAtBetweenOrderByVisitedAt(
            userId, startDate, endDate);

        // 일별 작성 여부 맵 생성
        Map<String, Boolean> dailyStatus = new HashMap<>();
        int daysInMonth = endDate.getDayOfMonth();

        // 모든 일을 false로 초기화
        for (int day = 1; day <= daysInMonth; day++) {
            dailyStatus.put(String.valueOf(day), false);
        }

        // 작성된 일기를 true로 설정
        for (Diary diary : diaries) {
            int day = diary.getVisitedAt().getDayOfMonth();
            dailyStatus.put(String.valueOf(day), true);
        }

        return DiaryWritingStatusResponseDto.from(userId, year, month, dailyStatus);
    }

    //일별 작성한 일기 목록 조회
    public DailyDiaryListResponseDto getDailyDiaries(UUID userId, LocalDate date) {
        List<Diary> diaries = diaryRepository.findByUserIdAndVisitedAtOrderByCreatedAtDesc(userId, date);

        List<DiaryResponseDto> diaryList = diaries.stream()
            .map(DiaryResponseDto::from)
            .toList();

        return DailyDiaryListResponseDto.from(userId, date, diaryList);
    }

    //축제별 일기 조회
    public EventDiaryResponseDto getDiariesByEventId(UUID eventId, Pageable pageable) {
        // 축제 존재 여부 확인
        Event event = eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);

        Page<Diary> diaryPage = diaryRepository.findPublicDiariesByEventId(eventId, pageable);

        List<DiaryResponseDto> diaries = diaryPage.getContent().stream()
            .map(DiaryResponseDto::from)
            .toList();

        return EventDiaryResponseDto.from(event, diaries);
    }

    //특정 유저의 일기 목록 조회
    public Page<DiaryResponseDto> getUserDiaries(UUID userId, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(UsernameNotFoundException::new);

        Page<Diary> diaries = diaryRepository.findPublicByUserIdOrderByVisitedAtDesc(userId, pageable);

        return diaries.map(DiaryResponseDto::from);
    }

}
