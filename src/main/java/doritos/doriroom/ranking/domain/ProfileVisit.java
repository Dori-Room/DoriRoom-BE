package doritos.doriroom.ranking.domain;

import doritos.doriroom.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profile_visits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProfileVisit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id", nullable = false)
    private User visitor; // 방문자
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visited_user_id", nullable = false)
    private User visitedUser; // 방문당한 유저

    @Column(nullable = false)
    private LocalDateTime visitedAt;

    @PrePersist
    protected void onVisit() {
        if (visitedAt == null) {
            visitedAt = LocalDateTime.now();
        }
    }

    @Builder
    public ProfileVisit(User visitor, User visitedUser) {
        this.visitor = visitor;
        this.visitedUser = visitedUser;
    }
} 