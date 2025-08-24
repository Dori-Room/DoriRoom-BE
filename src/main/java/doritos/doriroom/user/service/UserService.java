package doritos.doriroom.user.service;

import doritos.doriroom.auth.exception.InvalidPasswordException;
import doritos.doriroom.item.dto.response.EquippedItemResponse;
import doritos.doriroom.item.service.ItemService;
import doritos.doriroom.s3.S3Uploader;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.dto.request.ChangePasswordRequestDto;
import doritos.doriroom.user.dto.request.UpdateProfileRequestDto;
import doritos.doriroom.user.dto.response.OtherUserRoomResponseDto;
import doritos.doriroom.user.dto.response.ProfileImageResponseDto;
import doritos.doriroom.user.dto.response.UserCreditResponseDto;
import doritos.doriroom.user.dto.response.UserMyPageInfoDetailResponseDto;
import doritos.doriroom.user.exception.DuplicateException;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
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

    //다른 유저의 방 정보
    @Transactional(readOnly = true)
    public OtherUserRoomResponseDto getOhterUserRoomInfo(UUID userId, UUID targetUserId){
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("자신의 방 정보는 이 API로 조회할 수 없습니다.");
        }

        User targetUser = userRepository.findByUserId(targetUserId)
            .orElseThrow(UserNotFoundException::new);

        List<EquippedItemResponse> equippedItems = itemService.getOtherUserEquippedItems(targetUserId);

        incrementViewCount(targetUserId);
        return OtherUserRoomResponseDto.from(targetUser, equippedItems);
    }

    //조회수
    @Transactional
    public void incrementViewCount(UUID userId){
        User user = userRepository.findByUserId(userId).orElseThrow(UserNotFoundException::new);
        user.setViewCount(user.getViewCount()+1);
        userRepository.save(user);
    }
}
