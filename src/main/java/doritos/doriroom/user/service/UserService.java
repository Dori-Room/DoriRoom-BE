package doritos.doriroom.user.service;

import doritos.doriroom.atlas.repository.UserAtlasRepository;
import doritos.doriroom.atlas.repository.UserAtlasRewardRepository;
import doritos.doriroom.auth.exception.InvalidPasswordException;
import doritos.doriroom.auth.repository.RefreshTokenRedisRepository;
import doritos.doriroom.challenge.domain.challenge.ChallengeType;
import doritos.doriroom.challenge.repository.UserChallengeRepository;
import doritos.doriroom.challenge.service.ChallengeService;
import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.diary.domain.DiaryLike;
import doritos.doriroom.diary.repository.DiaryLikeRepository;
import doritos.doriroom.diary.repository.DiaryRepository;
import doritos.doriroom.follow.domain.Follow;
import doritos.doriroom.follow.dto.request.UserSearchRequestDto;
import doritos.doriroom.guestbook.repository.GuestbookRepository;
import doritos.doriroom.item.repository.UserItemRepository;
import doritos.doriroom.notification.repository.NotificationRepository;
import doritos.doriroom.user.domain.RoomLike;
import doritos.doriroom.user.dto.request.FcmTokenRequestDto;
import doritos.doriroom.ranking.service.ProfileVisitService;
import doritos.doriroom.user.dto.request.SpeechBubbleRequest;
import doritos.doriroom.user.dto.request.UserWithdrawalRequestDto;
import doritos.doriroom.user.dto.response.*;
import doritos.doriroom.follow.repository.FollowRepository;
import doritos.doriroom.item.dto.response.EquippedItemResponse;
import doritos.doriroom.item.service.ItemService;
import doritos.doriroom.s3.S3Uploader;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.dto.request.ChangePasswordRequestDto;
import doritos.doriroom.user.dto.request.UpdateProfileRequestDto;
import doritos.doriroom.user.exception.DuplicateException;
import doritos.doriroom.user.exception.SelfRoomInfoNotAllowedException;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.RoomLikeRepository;
import doritos.doriroom.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final S3Uploader s3Uploader;
    private final ItemService itemService;
    private final ChallengeService challengeService;
    private final ProfileVisitService profileVisitService;

    // 탈퇴 시 삭제 관련
    private final FollowRepository followRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;
    private final RoomLikeRepository roomLikeRepository;
    private final GuestbookRepository guestbookRepository;
    private final UserItemRepository userItemRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserAtlasRepository userAtlasRepository;
    private final UserAtlasRewardRepository userAtlasRewardRepository;
    private final NotificationRepository notificationRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;


    public void checkUsernameDuplicate(String username){
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateException("아이디");
        }
    }
    public void checkNicknameDuplicate(String nickname){
        if (userRepository.existsByNickname(nickname)) {
            throw new DuplicateException("닉네임");
        }
    }

    // 내 정보 조회 (마이페이지)
    @Transactional(readOnly = true)
    public UserMyPageInfoDetailResponseDto getUserInfoDetail(User user){
        User foundUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        return UserMyPageInfoDetailResponseDto.from(foundUser);
    }

    // 내 크레딧 조회
    @Transactional(readOnly = true)
    public UserCreditResponseDto getUserCredit(User user) {
        User foundUser =  userRepository.findByUserId(user.getUserId())
                .orElseThrow(UserNotFoundException::new);
        return  UserCreditResponseDto.from(foundUser);
    }

    // 닉네임 변경
    @Transactional
    public void updateProfile(User user, UpdateProfileRequestDto request){
        User foundUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (!foundUser.getNickname().equals(request.nickname())){ // 닉네임 변경 여부 확인
            checkNicknameDuplicate(request.nickname()); // 닉네임 중복 검사
        }

        foundUser.setNickname(request.nickname()); // 닉네임 업데이트
    }

    // 방 말풍선 변경
    @Transactional
    public void updateSpeechBubble(User user, SpeechBubbleRequest request){
        User foundUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        foundUser.setSpeechBubble(request.speechBubble());
    }

    // 프로필 이미지 변경
    @Transactional
    public ProfileImageResponseDto uploadProfileImage(User user, MultipartFile file){
        User foundUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (StringUtils.hasText(foundUser.getProfileImageUrl())) { // 기존 프로필 이미지가 있으면, s3에서 삭제
            s3Uploader.deleteFile(foundUser.getProfileImageUrl());
        }

        String profileImageUrl = s3Uploader.uploadFile(file, "profile_image"); // s3에 업로드 후 url 반환

        foundUser.setProfileImageUrl(profileImageUrl); // 새 이미지 url 반영

        return new ProfileImageResponseDto(profileImageUrl);
    }

    // 프로필 이미지 삭제
    @Transactional
    public void deleteProfileImage(User user){
        User foundUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (StringUtils.hasText(foundUser.getProfileImageUrl())) { // 기존 프로필 이미지가 있는지 확인 없으면 수행하지 않음
            s3Uploader.deleteFile(foundUser.getProfileImageUrl()); // s3에서 삭제
            foundUser.setProfileImageUrl(null); // DB에서 삭제
        }
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(User user, ChangePasswordRequestDto request) {
        User foundUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (!encoder.matches(request.currentPassword(), foundUser.getPassword())){ // 기존 비밀번호와 현재 입력한 비밀번호가 일치한지 확인
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (!request.newPassword().equals(request.confirmPassword())){
            throw new InvalidPasswordException("새 비밀번호가 일치하지 않습니다.");
        }

        foundUser.setPassword(encoder.encode(request.newPassword())); // 새 비밀번호를 암호화하여 저장
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(User user, UserWithdrawalRequestDto request){
        User foundUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (!encoder.matches(request.password(), foundUser.getPassword())){
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        deleteLikesHandler(foundUser); // 다른 유저에게 누른 좋아요들 삭제

        // 유저 관계 삭제
        guestbookRepository.deleteAllByUserId(foundUser.getUserId());
        followRepository.deleteAllByUser(foundUser);

        // 유저 개인 데이터 삭제
        userChallengeRepository.deleteAllByUser(foundUser);
        userAtlasRewardRepository.deleteAllByUser(foundUser);
        userAtlasRepository.deleteAllByUser(foundUser);
        userItemRepository.deleteAllByUser(foundUser);
        notificationRepository.deleteAllByUser(foundUser);
        refreshTokenRedisRepository.deleteById(foundUser.getUserId());

        deleteUserDiariesAndRelatedData(foundUser); // 유저 생성 데이터 삭제
        roomLikeRepository.deleteAllByRoomOwner(foundUser); // 유저의 방 좋아요 삭제


        userRepository.delete(foundUser); // 유저 삭제

        // 기존 프로필 이미지가 있으면 S3에서 삭제
        if (StringUtils.hasText(foundUser.getProfileImageUrl())) {
            s3Uploader.deleteFile(foundUser.getProfileImageUrl());
        }
    }

    //내 방 정보
    @Transactional(readOnly = true)
    public MyRoomResponseDto getMyRoomInfo(User user) {
        User foundUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        // 내가 착용 중인 아이템 조회
        List<EquippedItemResponse> equippedItems = itemService.getEquippedItems(user);

        return MyRoomResponseDto.from(foundUser, equippedItems);
    }

    //다른 유저의 방 정보
    @Transactional
    public OtherUserRoomResponseDto getOtherUserRoomInfo(UUID userId, UUID targetUserId){
        if (userId.equals(targetUserId)) {
            throw new SelfRoomInfoNotAllowedException();
        }

        User targetUser = userRepository.findByUserId(targetUserId)
                .orElseThrow(UserNotFoundException::new);

        List<EquippedItemResponse> equippedItems = itemService.getOtherUserEquippedItems(targetUserId);

        int viewCount = incrementViewCount(targetUserId);

        // 다른 사람의 방 N번 방문하기 과제에 반영
        User user = userRepository.findByUserId(userId).orElseThrow(UserNotFoundException::new);
        challengeService.updateChallengeProgress(user, ChallengeType.VISIT_NEIGHBOR, 1);

        // 특정 유저의 총 방문 수 N번 달성 과제에 반영
        challengeService.updateChallengeProgressCount(targetUser, ChallengeType.VISIT_NEIGHBOR, viewCount);

        // 프로필 방문 기록 추가
        profileVisitService.addProfileVisit(user, targetUser);

        return OtherUserRoomResponseDto.from(targetUser, equippedItems);
    }

    //조회수
    @Transactional
    public int incrementViewCount(UUID userId){
        User user = userRepository.findByUserId(userId).orElseThrow(UserNotFoundException::new);
        user.setViewCount(user.getViewCount()+1);
        userRepository.save(user);

        return user.getViewCount();
    }

    // 유저 검색
    public List<UserSearchResultDto> searchUsers(User user, UserSearchRequestDto request) {
        // 닉네임으로 유저 검색 (자기 자신 제외)
        List<User> foundUsers = userRepository
                .findByNicknameContainingIgnoreCaseAndUserIdNot(request.keyword(), user.getUserId());

        if (foundUsers.isEmpty()) return List.of();

        Set<UUID> foundUserIds = foundUsers.stream()
                .map(User::getUserId)
                .collect(Collectors.toSet());

        // 내가 팔로우하는 관계
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(user, foundUserIds)
                .stream()
                .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));

        // 나를 팔로우하는 관계
        Set<UUID> followedByMeUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(user, foundUserIds);


        return foundUsers.stream()
                .map(targetUser -> {
                    UUID targetUserId = targetUser.getUserId();
                    Follow following = followingMap.get(targetUserId);

                    boolean isFollowing = following != null;
                    boolean isBestFriend = following != null && following.isBestFriend();
                    boolean isFollowedBy = followedByMeUserIds.contains(targetUserId);

                    return new UserSearchResultDto(
                            targetUserId,
                            targetUser.getNickname(),
                            targetUser.getProfileImageUrl(),
                            isFollowing,
                            isFollowedBy,
                            isBestFriend
                    );
                })
                .toList();
    }
  
    // fcm 토큰 등록 및 업데이트
    @Transactional
    public void updateFcmToken(User user, FcmTokenRequestDto request) {
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        foundUser.setFcmToken(request.fcmToken()); // fcm 토큰 업데이트
    }

    // fcm 토큰 삭제
    @Transactional
    public void deleteFcmToken(User user) {
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        foundUser.setFcmToken(null);
    }



    /*  내부 메서드  */

    // 유저가 누른 좋아요 삭제하여 관련 게시물의 카운트를 감소
    private void deleteLikesHandler(User user) {
        // 내가 좋아요 누른 다이어리 목록 조회 후 카운트 감소 및 삭제
        List<DiaryLike> diaryLikes = diaryLikeRepository.findByUser(user);
        for (DiaryLike like : diaryLikes) {
            like.getDiary().decrementLikes();
        }
        diaryLikeRepository.deleteAll(diaryLikes);

        // 내가 좋아요 누른 방
        List<RoomLike> roomLikes = roomLikeRepository.findByLiker(user);
        for (RoomLike like : roomLikes) {
            like.getRoomOwner().decrementLikeCount();
        }
        roomLikeRepository.deleteAll(roomLikes);
    }

    // 유저가 작성한 모든 일기와 관련된 데이터 식제
    private void deleteUserDiariesAndRelatedData(User user) {
        // 모든 다이어리 조회
        List<Diary> userDiaries = diaryRepository.findAllByUserId(user.getUserId());
        if (userDiaries.isEmpty()) {
            return;
        }

        // 다이어리 id 리스트 추출
        List<UUID> diaryIds = userDiaries.stream()
                .map(Diary::getDiaryId)
                .collect(Collectors.toList());

        // 해당 다이어리 전부 삭제
        diaryLikeRepository.deleteAllByDiaryIds(diaryIds);

        // S3에서 관련 이미지들을 삭제
        for (Diary diary : userDiaries) {
            if (diary.getImageUrls() != null && !diary.getImageUrls().isEmpty()) {
                s3Uploader.deleteFiles(diary.getImageUrls());
            }
        }

        diaryRepository.deleteAll(userDiaries); // 최종 다이어리 삭제
    }
}