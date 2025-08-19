package doritos.doriroom.challenge.domain.challenge;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.item.domain.Item;
import doritos.doriroom.tourApi.domain.AreaGroup;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

import java.util.UUID;

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

    @Column(nullable = false)
    private Long rewardCredits; // 보상 크레딧

//    @Column(nullable = false)
//    private Long rewardExps; // 보상 경험치 -> 도감 달성도에 반영
    // 경험치는 지역별로 다르게 주어야함

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ChallengeType challengeType; // 과제 형식. (예: 축제 방문, 지역 퀴즈, 일기 10개 작성)

    @Builder.Default @Column(nullable = false)
    private int targetCount = 1; // n회성 과제이면 설정 (예: 축제 방문 과제(1회성) -> 1, 일기 10개 쓰기 과제 -> 10)

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ChallengeGroup challengeGroup; //  지역과제인지 일반과제인지

    @Enumerated(EnumType.STRING)
    private AreaGroup areaGroup; // 지역과제라면 (challengeGroup == AREA)
                                    // 지역 값 포함. (일반 과제인 경우 null)

//    private UUID itemId;
//    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // 과제에 보상 아이템이 있는 경우 값을 가짐

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event; // 과제에 관련 축제가 있는 경우 값을 가짐, 해당 이벤트 아이디로 지도에 표시할 범위 위치 리스트를 가져옴

}
