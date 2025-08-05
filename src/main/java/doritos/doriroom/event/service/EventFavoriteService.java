package doritos.doriroom.event.service;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.domain.EventFavorite;
import doritos.doriroom.event.dto.response.EventResponseDto;
import doritos.doriroom.event.exception.EventFavoriteException;
import doritos.doriroom.event.exception.EventNotFoundException;
import doritos.doriroom.event.repository.EventFavoriteRepository;
import doritos.doriroom.event.repository.EventRepository;
import doritos.doriroom.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventFavoriteService {

    private final EventFavoriteRepository eventFavoriteRepository;
    private final EventRepository eventRepository;

    @Transactional
    public boolean toggleFavorite(User user, UUID eventId) {
        try {
            Event event = eventRepository.findById(eventId)
                .orElseThrow(EventNotFoundException::new);

            Optional<EventFavorite> existingLike = eventFavoriteRepository.findByUserIdAndEventId(user.getUserId(), eventId);

            if (existingLike.isPresent()) {
                // 좋아요 취소
                eventFavoriteRepository.delete(existingLike.get());
                event.decreaseFavoriteCount();
                eventRepository.save(event);
                return false;
            } else {
                // 좋아요 추가
                EventFavorite like = EventFavorite.builder()
                    .user(user)
                    .event(event)
                    .build();

                eventFavoriteRepository.save(like);
                event.increaseFavoriteCount();
                eventRepository.save(event);
                return true;
            }
        } catch (EventNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new EventFavoriteException();
        }
    }

    public boolean isLiked(User user, UUID eventId) {
        return eventFavoriteRepository.findByUserIdAndEventId(user.getUserId(), eventId).isPresent();
    }

    public Page<EventResponseDto> getUserFavoriteEvents(User user, Pageable pageable) {
        Page<EventFavorite> favorites = eventFavoriteRepository.findFavoritesWithEventByUserId(user.getUserId(), pageable);

        return favorites.map(favorite -> EventResponseDto.from(favorite.getEvent()));
    }
}