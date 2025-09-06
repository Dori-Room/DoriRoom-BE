package doritos.doriroom.challenge.domain.challenge;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.tourApi.domain.AreaGroup;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenges")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Challenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // challengeId

    @Column(nullable = false)
    private String title; // (예: 이웃 집 방문 5회)
    private String content; // 필요 시 사용

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ChallengeGroup challengeGroup; //  지역과제인지 일반과제인지

    @Enumerated(EnumType.STRING)
    private AreaGroup areaGroup; // 지역과제라면 (challengeGroup == AREA) 지역 값을 포함. (일반 과제인 경우 null)

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ChallengeType challengeType; // 과제 형식 (예: 축제 방문, 지역 퀴즈, 일기 10개 작성)

    @Builder.Default @Column(nullable = false)
    private int targetCount = 1; // n회성 과제이면 설정 (예: 축제 방문 과제(1회성) -> 1, 일기 10개 쓰기 과제 -> 10)

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeReward> rewards = new ArrayList<>(); // 해당 도전과제에 대한 보상들 리스트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event; // 과제에 관련 축제가 있다면 값을 포함

    @Column(columnDefinition = "LONGTEXT")
    private String polygon;

    private LocalDate startDate; // 필요 시 기한을 설정 (예: 축제 관련 과제)
    private LocalDate endDate;

}
