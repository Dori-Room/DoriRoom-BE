package doritos.doriroom.diary.service;

import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.diary.dto.request.*;
import doritos.doriroom.diary.dto.response.*;
import doritos.doriroom.diary.exception.*;
import doritos.doriroom.diary.repository.DiaryRepository;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.response.EventDiaryResponseDto;
import doritos.doriroom.event.exception.EventNotFoundException;
import doritos.doriroom.event.repository.EventRepository;
import doritos.doriroom.user.domain.RoomVisibility;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Event event = eventRepository.findById(request.eventId()).orElseThrow(EventNotFoundException::new);

        Diary diary = Diary.from(userId, request);

        //축제의 일기 count 증가
        if (request.visibility() == RoomVisibility.PUBLIC) {
            event.incrementDiaryCount();
            eventRepository.save(event);
        }
        diaryRepository.save(diary);

        return DiaryResponseDto.from(diary, user, event);
    }

    @Transactional
    public DiaryResponseDto updateDiary(UUID userId, UUID diaryId, DiaryUpdateRequestDto request) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Event event = eventRepository.findById(diary.getEventId()).orElseThrow(EventNotFoundException::new);

        if (!diary.getUserId().equals(userId)) {
            throw new DiaryAuthorizationException();
        }

        // 공개 설정이 변경된 경우 축제의 diary count 변경
        if (request.visibility() != null && !request.visibility().equals(diary.getDiaryVisibility())) {
            if (request.visibility() == RoomVisibility.PUBLIC && diary.getDiaryVisibility() == RoomVisibility.PRIVATE) {
                // 비공개 → 공개로 변경: 카운트 증가
                event.incrementDiaryCount();
            } else if (request.visibility() == RoomVisibility.PRIVATE && diary.getDiaryVisibility() == RoomVisibility.PUBLIC) {
                // 공개 → 비공개로 변경: 카운트 감소
                event.decrementDiaryCount();
            }
            eventRepository.save(event);
        }

        diary.updateDiary(request);
        Diary updatedDiary = diaryRepository.save(diary);

        return DiaryResponseDto.from(updatedDiary, user, event);
    }

    @Transactional
    public void deleteDiary(UUID userId, UUID diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);

        if(!diary.getUserId().equals(userId)) {
            throw new DiaryAuthorizationException();
        }

        if (diary.getDiaryVisibility() == RoomVisibility.PUBLIC) {
            Event event = eventRepository.findById(diary.getEventId())
                .orElseThrow(EventNotFoundException::new);
            event.decrementDiaryCount();
            eventRepository.save(event);
        }


        diaryRepository.delete(diary);
    }

    public DiaryDetailResponseDto getDiaryDetail(UUID diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
            .orElseThrow(DiaryNotFoundException::new);

        User user = userRepository.findById(diary.getUserId())
            .orElseThrow(UserNotFoundException::new);

        Event event = eventRepository.findById(diary.getEventId())
            .orElseThrow(EventNotFoundException::new);

        return DiaryDetailResponseDto.from(diary, user, event);
    }

    //월별 일기 작성 여부 조회
    public DiaryWritingStatusResponseDto getDiaryWritingStatus(UUID userId, int year, int month) {
        // 해당 월의 시작일과 종료일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Diary> diaries = diaryRepository.findByUserIdAndVisitedAtBetweenOrderByVisitedAt(
            userId, startDate, endDate);

        return createDiaryWritingStatus(userId, year, month, diaries);
    }

    //다른 유저의 월별 일기 작성 여부 조회
    public DiaryWritingStatusResponseDto getOtherUserDiaryWritingStatus(UUID userId, int year, int month) {
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        // 해당 월의 시작일과 종료일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        /**
         * 추후 여기에 follow 관계인지 확인하는 로직 추가
         * 현재는 다른 유저의 public 일기만 조회
         */

        // 해당 월에 작성된 public 일기만 조회
        List<Diary> diaries = diaryRepository.findPublicByUserIdAndVisitedAtBetweenOrderByVisitedAt(
            userId, startDate, endDate);

        return createDiaryWritingStatus(userId, year, month, diaries);
    }

    // 일기 작성 여부 상태 생성 공통 메서드
    private DiaryWritingStatusResponseDto createDiaryWritingStatus(UUID userId, int year, int month, List<Diary> diaries) {
        Map<String, Boolean> dailyStatus = new HashMap<>();

        LocalDate startDate = LocalDate.of(year, month, 1);
        int daysInMonth = startDate.lengthOfMonth();

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

        if (diaries.isEmpty()) {
            return DailyDiaryListResponseDto.from(userId, date, List.of());
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        List<UUID> eventIds = diaries.stream()
            .map(Diary::getEventId)
            .distinct()
            .toList();

        Map<UUID, Event> eventMap = eventRepository.findByEventIdIn(eventIds).stream()
            .collect(Collectors.toMap(Event::getEventId, event -> event));

        List<DiaryResponseDto> diaryList = diaries.stream()
            .map(diary -> {
                Event event = eventMap.get(diary.getEventId());
                return DiaryResponseDto.from(diary, user, event);
            })
            .toList();

        return DailyDiaryListResponseDto.from(userId, date, diaryList);
    }

    //축제별 일기 조회
    public EventDiaryResponseDto getDiariesByEventId(UUID eventId, Pageable pageable) {
        // 축제 존재 여부 확인
        Event event = eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);
        Page<Diary> diaryPage = diaryRepository.findPublicDiariesByEventId(eventId, pageable);

        if (diaryPage.isEmpty()) {
            return EventDiaryResponseDto.from(event, List.of());
        }

        List<UUID> userIds = diaryPage.getContent().stream()
            .map(Diary::getUserId)
            .distinct()
            .toList();

        Map<UUID, User> userMap = userRepository.findByUserIdIn(userIds).stream()
            .collect(Collectors.toMap(User::getUserId, user -> user));

        List<DiaryResponseDto> diaries = diaryPage.getContent().stream()
            .map(diary -> {
                User user = userMap.get(diary.getUserId());
                return DiaryResponseDto.from(diary, user, event);
            })
            .toList();

        return EventDiaryResponseDto.from(event, diaries);
    }

    //특정 유저의 일기 목록 조회
    public Page<DiaryResponseDto> getUserDiaries(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        Page<Diary> diaries = diaryRepository.findPublicByUserIdOrderByVisitedAtDesc(userId, pageable);

        if (diaries.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> eventIds = diaries.getContent().stream()
            .map(Diary::getEventId)
            .distinct()
            .toList();

        Map<UUID, Event> eventMap = eventRepository.findByEventIdIn(eventIds).stream()
            .collect(Collectors.toMap(Event::getEventId, event -> event));

        // Page의 content만 변환하고 Page 객체는 유지
        List<DiaryResponseDto> diaryResponseList = diaries.getContent().stream()
            .map(diary -> {
                Event event = eventMap.get(diary.getEventId());
                return DiaryResponseDto.from(diary, user, event);
            })
            .toList();

        // 새로운 Page 객체 생성
        return new PageImpl<>(diaryResponseList, pageable, diaries.getTotalElements());
    }

}
