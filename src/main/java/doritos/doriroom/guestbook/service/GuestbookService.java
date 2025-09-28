package doritos.doriroom.guestbook.service;

import doritos.doriroom.guestbook.domain.Guestbook;
import doritos.doriroom.guestbook.dto.request.GuestbookRequestDto;
import doritos.doriroom.guestbook.dto.response.GuestbookResponseDto;
import doritos.doriroom.guestbook.exception.GuestbookDeletionAuthorizationException;
import doritos.doriroom.guestbook.exception.GuestbookNotFoundException;
import doritos.doriroom.guestbook.exception.SelfGuestbookNotAllowedException;
import doritos.doriroom.guestbook.repository.GuestbookRepository;
import doritos.doriroom.item.dto.response.EquippedItemResponse;
import doritos.doriroom.item.service.ItemService;
import doritos.doriroom.notification.domain.NotificationType;
import doritos.doriroom.notification.service.NotificationService;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GuestbookService {
    private final GuestbookRepository guestbookRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;
    private final NotificationService notificationService;

    @Transactional
    public GuestbookResponseDto createGuestbook(UUID writerId, GuestbookRequestDto request) {
        // 방 주인 존재 여부 확인
        User roomOwner = userRepository.findById(request.roomOwnerId())
                .orElseThrow(UserNotFoundException::new);

        // 자신의 방에 방명록을 작성하려는 경우 예외 처리
        if (writerId.equals(request.roomOwnerId())) {
            throw new SelfGuestbookNotAllowedException();
        }

        User user = userRepository.findById(writerId).orElseThrow(UserNotFoundException::new);

        Guestbook guestbook = Guestbook.from(writerId, request);
        List<EquippedItemResponse> equippedItems = itemService.getOtherUserEquippedItems(writerId);

        Guestbook savedGuestbook = guestbookRepository.save(guestbook);

        // 방 주인에게 방명록 작성 알림 발송
        notificationService.sendNotification(roomOwner, NotificationType.GUESTBOOK_ENTRY, user.getNickname());

        return GuestbookResponseDto.from(savedGuestbook, user.getNickname(), equippedItems);
    }

    public Page<GuestbookResponseDto> getGuestbooksByRoomOwner(UUID roomOwnerId, Pageable pageable) {
        if (!userRepository.existsById(roomOwnerId)) {
            throw new UserNotFoundException();
        }

        Page<Guestbook> guestbooks = guestbookRepository.findByRoomOwnerIdOrderByCreatedAtDesc(roomOwnerId, pageable);

        return guestbooks.map(guestbook -> {
            User writer = userRepository.findById(guestbook.getWriterId()).orElseThrow(UserNotFoundException::new);

            List<EquippedItemResponse> equippedItems = itemService.getOtherUserEquippedItems(writer.getUserId());
            return GuestbookResponseDto.from(guestbook, writer.getNickname(), equippedItems);
        });
    }

    @Transactional
    public void deleteGuestbook(UUID guestbookId, User user) {
        Guestbook guestbook = guestbookRepository.findById(guestbookId)
                .orElseThrow(GuestbookNotFoundException::new);

        // 작성자 또는 방 주인만 삭제 가능
        if (!guestbook.getWriterId().equals(user.getUserId()) && 
            !guestbook.getRoomOwnerId().equals(user.getUserId())) {
            throw new GuestbookDeletionAuthorizationException();
        }

        guestbookRepository.delete(guestbook);
    }
}
