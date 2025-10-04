package doritos.doriroom.notification.repository;

import doritos.doriroom.notification.domain.Notification;
import doritos.doriroom.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable); // 알림 목록을 최신순으로 페이징
    List<Notification> findByUserAndReadFalse(User user);
}
