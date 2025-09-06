package doritos.doriroom.diary.service;

import doritos.doriroom.challenge.domain.challenge.ChallengeType;
import doritos.doriroom.challenge.service.ChallengeService;
import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.diary.dto.request.*;
import doritos.doriroom.diary.dto.response.*;
import doritos.doriroom.diary.exception.*;
import doritos.doriroom.diary.repository.DiaryLikeRepository;
import doritos.doriroom.diary.repository.DiaryRepository;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.response.EventDiaryResponseDto;
import doritos.doriroom.event.exception.EventNotFoundException;
import doritos.doriroom.event.repository.EventRepository;
import doritos.doriroom.follow.service.FollowService;
import doritos.doriroom.s3.S3Uploader;
import doritos.doriroom.user.domain.RoomVisibility;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final S3Uploader s3Uploader;
    private final ChallengeService challengeService;
    private final FollowService followService;

    private static final Long DIARY_WRITE_BASE_CREDIT = 3L;
    private static final Long PHOTO_ATTACHMENT_BONUS_CREDIT = 2L;

    @Transactional
    public DiaryResponseDto createDiary(User user, DiaryCreateRequestDto request, List<MultipartFile> images){
        userRepository.findById(user.getUserId()).orElseThrow(UserNotFoundException::new);

        Event event = eventRepository.findById(request.eventId()).orElseThrow(EventNotFoundException::new);

        boolean alreadyWroteDiary = diaryRepository.existsByUserIdAndEventId(user.getUserId(), request.eventId());
        if (alreadyWroteDiary) {
            throw new DuplicateDiaryException();
        }

        //S3에 이미지 업로드
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            String path = "diary/" + user.getUserId().toString();
            imageUrls = s3Uploader.uploadFiles(images, path);
        }

        Diary diary = Diary.from(user.getUserId(), request, imageUrls);

        //축제의 일기 count 증가
        if (request.visibility() == RoomVisibility.PUBLIC) {
            event.incrementDiaryCount();
            eventRepository.save(event);
        }

        diaryRepository.save(diary);

        // 포인트 지급
        Long totalCredit = calculateDiaryCredit(imageUrls);
        user.addCredit(totalCredit);
        userRepository.save(user);

        // 일반 과제 진행에 반영 (진행도 +1)
        challengeService.updateChallengeProgress(user, ChallengeType.WRITE_DIARY, 1);

        return DiaryResponseDto.from(diary, totalCredit, user, event);
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

        return DiaryResponseDto.from(updatedDiary, 0L, user, event);
    }

    @Transactional
    public void deleteDiary(UUID userId, UUID diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if(!diary.getUserId().equals(userId)) {
            throw new DiaryAuthorizationException();
        }

        // 일기 삭제 전에 관련된 좋아요들을 먼저 삭제
        diaryLikeRepository.deleteByDiaryId(diaryId);

        if (diary.getImageUrls() != null && !diary.getImageUrls().isEmpty()) {
            s3Uploader.deleteFiles(diary.getImageUrls());
        }

        if (diary.getDiaryVisibility() == RoomVisibility.PUBLIC) {
            Event event = eventRepository.findById(diary.getEventId())
                .orElseThrow(EventNotFoundException::new);
            event.decrementDiaryCount();
            eventRepository.save(event);
        }

        diaryRepository.delete(diary);

        // 일반 과제 진행에 반영 (진행도 -1)
        challengeService.updateChallengeProgress(user, ChallengeType.WRITE_DIARY, -1);
    }

    public DiaryDetailResponseDto getDiaryDetail(UUID currentUserId,UUID diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
            .orElseThrow(DiaryNotFoundException::new);

        User user = userRepository.findById(diary.getUserId())
            .orElseThrow(UserNotFoundException::new);

        Event event = eventRepository.findById(diary.getEventId())
            .orElseThrow(EventNotFoundException::new);

        // 권한 검증
        boolean isOwnDiary = currentUserId.equals(diary.getUserId());

        if (!isOwnDiary) {
            boolean isBestFriend = followService.isBestFriendByDiaryWriter(diary.getUserId(), currentUserId);

            if (!isBestFriend && diary.getDiaryVisibility() != RoomVisibility.PUBLIC) {
                // 단짝이 아니고 공개 일기가 아닌 경우 접근 불가
                throw new DiaryAuthorizationException();
            }

            if (isBestFriend && diary.getDiaryVisibility() == RoomVisibility.PRIVATE) {
                // 단짝이라도 비공개 일기는 접근 불가
                throw new DiaryAuthorizationException();
            }
        }

        return DiaryDetailResponseDto.from(diary, user, event);
    }

    //월별 일기 작성 여부 조회
    public DiaryWritingStatusResponseDto getUserDiaryWritingStatus(UUID currentUserId, UUID targetUserId, int year, int month) {
        boolean isOwner = currentUserId.equals(targetUserId);

        LocalDate startDate = LocalDate.of(year, month, 1);
        int lastDayOfMonth = startDate.lengthOfMonth();
        LocalDate endDate = LocalDate.of(year, month, lastDayOfMonth);

        List<Diary> diaries;
        if (isOwner) {
            // 자신의 일기
            diaries = diaryRepository.findByUserIdAndVisitedAtBetweenOrderByVisitedAt(
                targetUserId, startDate, endDate);
        } else {
            // 다른 사용자의 일기
            boolean isBestFriend = followService.isBestFriendByDiaryWriter(targetUserId, currentUserId);

            if(isBestFriend) {
                // 일기 작성자가 조회자를 베스트프렌드로 설정한 경우
                diaries = diaryRepository.findPublicAndFollowersByUserIdAndVisitedAtBetweenOrderByVisitedAt(
                    targetUserId,
                    List.of(RoomVisibility.PUBLIC, RoomVisibility.FOLLOWERS),
                    startDate,
                    endDate);
            } else {
                // 단짝친구 아닌 경우
                diaries = diaryRepository.findPublicByUserIdAndVisitedAtBetweenOrderByVisitedAt(
                    targetUserId, startDate, endDate);
            }
        }

        // 해당 월의 모든 날짜에 대해 초기화
        Map<String, DiaryWritingStatusResponseDto.DailyStatusInfo> dailyStatusMap = new LinkedHashMap<>();
        for (int day = 1; day <= lastDayOfMonth; day++) {
            dailyStatusMap.put(String.valueOf(day), DiaryWritingStatusResponseDto.DailyStatusInfo.notWritten());
        }

        // 조회된 일기 정보를 바탕으로 상태 업데이트
        for (Diary diary : diaries) {
            String dayKey = String.valueOf(diary.getVisitedAt().getDayOfMonth());

            String representativeImageUrl = null;
            if (diary.getImageUrls() != null && !diary.getImageUrls().isEmpty()) {
                representativeImageUrl = diary.getImageUrls().get(0);
            }

            dailyStatusMap.put(
                dayKey,
                new DiaryWritingStatusResponseDto.DailyStatusInfo(true, representativeImageUrl)
            );
        }

        return DiaryWritingStatusResponseDto.from(targetUserId, year, month, dailyStatusMap);
    }

    //일별 작성한 일기 목록 조회
//    public DailyDiaryListResponseDto getDailyDiaries(UUID userId, LocalDate date) {
//        List<Diary> diaries = diaryRepository.findByUserIdAndVisitedAtOrderByCreatedAtDesc(userId, date);
//
//        if (diaries.isEmpty()) {
//            return DailyDiaryListResponseDto.from(userId, date, List.of());
//        }
//
//        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
//
//        List<UUID> eventIds = diaries.stream()
//            .map(Diary::getEventId)
//            .distinct()
//            .toList();
//
//        Map<UUID, Event> eventMap = eventRepository.findByEventIdIn(eventIds).stream()
//            .collect(Collectors.toMap(Event::getEventId, event -> event));
//
//        List<DiaryResponseDto> diaryList = diaries.stream()
//            .map(diary -> {
//                Event event = eventMap.get(diary.getEventId());
//
//                return DiaryResponseDto.from(diary, 0L, user, event);
//            })
//            .toList();
//
//        return DailyDiaryListResponseDto.from(userId, date, diaryList);
//    }

    public DailyDiaryListResponseDto getDailyDiaries(UUID currentUserId, UUID targetUserId, LocalDate date) {
        userRepository.findById(currentUserId).orElseThrow(UserNotFoundException::new);
        User targetUser = userRepository.findById(targetUserId).orElseThrow(UserNotFoundException::new);

        List<Diary> diaries;

        // 자신의 일기인지 확인
        boolean isOwnDiary = currentUserId.equals(targetUserId);

        if (isOwnDiary) {
            // 자신의 일기 - 모든 일기 조회
            diaries = diaryRepository.findByUserIdAndVisitedAtOrderByCreatedAtDesc(targetUserId, date);
        } else {
            // 다른 사용자의 일기 - 단짝 여부에 따라 조회 범위 결정
            boolean isBestFriend = followService.isBestFriendByDiaryWriter(targetUserId, currentUserId);

            if (isBestFriend) {
                // 일기 작성자가 조회자를 단짝으로 설정한 경우
                diaries = diaryRepository.findPublicAndFollowersByUserIdAndVisitedAtOrderByCreatedAtDesc(
                    targetUserId,
                    List.of(RoomVisibility.PUBLIC, RoomVisibility.FOLLOWERS),
                    date);
            } else {
                // 단짝친구 아닌 경우 - 공개 일기만 조회
                diaries = diaryRepository.findPublicByUserIdAndVisitedAtOrderByCreatedAtDesc(targetUserId, date);
            }
        }

        if (diaries.isEmpty()) {
            return DailyDiaryListResponseDto.from(targetUserId, date, List.of());
        }

        List<UUID> eventIds = diaries.stream()
            .map(Diary::getEventId)
            .distinct()
            .toList();

        Map<UUID, Event> eventMap = eventRepository.findByEventIdIn(eventIds).stream()
            .collect(Collectors.toMap(Event::getEventId, event -> event));

        List<DiaryResponseDto> diaryList = diaries.stream()
            .map(diary -> {
                Event event = eventMap.get(diary.getEventId());
                return DiaryResponseDto.from(diary, 0L, targetUser, event);
            })
            .toList();

        return DailyDiaryListResponseDto.from(targetUserId, date, diaryList);
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

                return DiaryResponseDto.from(diary, 0L, user, event);
            })
            .toList();

        return EventDiaryResponseDto.from(event, diaries);
    }

    //특정 유저의 일기 목록 조회
    public Page<DiaryResponseDto> getUserDiaries(UUID currentUserId, UUID targetUserId, Pageable pageable) {
        User targetUser = userRepository.findById(targetUserId).orElseThrow(UserNotFoundException::new);

        //자신의 일기인지 확인
        boolean isOwnDiary = currentUserId.equals(targetUserId);

        Page<Diary> diaries = isOwnDiary ?
            diaryRepository.findByUserIdOrderByCreatedAtDesc(targetUserId, pageable) :
            diaryRepository.findPublicByUserIdOrderByVisitedAtDesc(targetUserId, pageable);

        return createDiaryResponsePage(diaries, targetUser, pageable);
    }

    public Diary getDiaryById(UUID diaryId) {
        return diaryRepository.findById(diaryId)
            .orElseThrow(DiaryNotFoundException::new);
    }

    //일기 작성 시 포인트 지급
    private Long calculateDiaryCredit(List<String> imageUrls) {
        Long credit = DIARY_WRITE_BASE_CREDIT;


        if (imageUrls != null && !imageUrls.isEmpty()) {
            credit += PHOTO_ATTACHMENT_BONUS_CREDIT;
        }

        return credit;
    }

    //자신의 일기 조회
    private Page<DiaryResponseDto> createDiaryResponsePage(Page<Diary> diaries, User user, Pageable pageable) {
        if (diaries.isEmpty()) {
            return Page.empty(pageable);
        }

        // 이벤트 정보 조회
        List<UUID> eventIds = diaries.getContent().stream()
            .map(Diary::getEventId)
            .distinct()
            .toList();

        Map<UUID, Event> eventMap = eventRepository.findByEventIdIn(eventIds).stream()
            .collect(Collectors.toMap(Event::getEventId, event -> event));

        // DiaryResponseDto로 변환
        List<DiaryResponseDto> diaryResponseList = diaries.getContent().stream()
            .map(diary -> {
                Event event = eventMap.get(diary.getEventId());

                return DiaryResponseDto.from(diary, 0L, user, event);
            })
            .toList();

        return new PageImpl<>(diaryResponseList, pageable, diaries.getTotalElements());
    }

    //이달의 인기글 조회
    public List<DiaryResponseDto> getPopularDiariesOfMonth() {
        // 현재 월의 시작과 끝 날짜 계산
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);

        // 인기글 조회 (좋아요 수 기준으로 정렬)
        Pageable pageable = PageRequest.of(0, 10);
        List<Diary> popularDiaries = diaryRepository.findPopularDiariesByMonth(
            startDateTime, endDateTime, pageable);

        if (popularDiaries.isEmpty()) {
            return List.of();
        }

        // 사용자와 이벤트 정보 배치 조회
        List<UUID> userIds = popularDiaries.stream()
            .map(Diary::getUserId)
            .distinct()
            .toList();

        List<UUID> eventIds = popularDiaries.stream()
            .map(Diary::getEventId)
            .distinct()
            .toList();

        Map<UUID, User> userMap = userRepository.findByUserIdIn(userIds).stream()
            .collect(Collectors.toMap(User::getUserId, user -> user));

        Map<UUID, Event> eventMap = eventRepository.findByEventIdIn(eventIds).stream()
            .collect(Collectors.toMap(Event::getEventId, event -> event));

        return popularDiaries.stream()
            .map(diary -> {
                User user = userMap.get(diary.getUserId());
                Event event = eventMap.get(diary.getEventId());

                return DiaryResponseDto.from(diary, 0L, user, event);
            })
            .toList();
    }

    public Page<DiaryResponseDto> getFriendsDiaries(UUID currentUserId, Pageable pageable) {
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

        // 내가 팔로우하는 모든 친구 ID와 나를 단짝으로 설정한 친구 ID 목록을 조회
        List<UUID> followingUserIds = followService.getFollowingIds(currentUserId);
        List<UUID> mutualBestFriendIds = followService.getMutualBestFriendIds(currentUserId);

        // 친구가 한 명도 없으면 빈 페이지 반환
        if (followingUserIds.isEmpty() && mutualBestFriendIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 단일 쿼리로 모든 조건에 맞는 일기를 DB에서 직접 페이징하여 조회
        Page<Diary> friendsDiariesPage = diaryRepository.findFriendsDiaries(
            followingUserIds,
            mutualBestFriendIds,
            twoWeeksAgo,
            pageable
        );

        // 사용자, 이벤트 정보 배치 조회 및 DTO 변환
        List<Diary> pagedDiaries = friendsDiariesPage.getContent();

        List<UUID> userIds = pagedDiaries.stream()
            .map(Diary::getUserId)
            .distinct()
            .toList();

        List<UUID> eventIds = pagedDiaries.stream()
            .map(Diary::getEventId)
            .distinct()
            .toList();

        // ID 목록이 비어있으면 불필요한 DB 조회를 막음
        Map<UUID, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
            userRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        Map<UUID, Event> eventMap = eventIds.isEmpty() ? Collections.emptyMap() :
            eventRepository.findByEventIdIn(eventIds).stream()
                .collect(Collectors.toMap(Event::getEventId, event -> event));

        // DTO 변환
        List<DiaryResponseDto> diaryResponses = pagedDiaries.stream()
            .map(diary -> {
                User diaryUser = userMap.get(diary.getUserId());
                Event event = eventMap.get(diary.getEventId());
                return DiaryResponseDto.from(diary, 0L, diaryUser, event); // 0L 부분은 필요시 로직 추가
            })
            .toList();

        return new PageImpl<>(diaryResponses, pageable, friendsDiariesPage.getTotalElements());
    }

    public boolean getUserWrittenDiaryForEvent(UUID userId, UUID eventId) {
        eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        return diaryRepository.existsByUserIdAndEventId(userId, eventId);
    }

}
