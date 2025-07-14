package doritos.doriroom.event.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "event")
@Getter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Event {
    @Id
    private UUID eventId;

    @Column
    private String firstImage;

    @Column
    private String secondImage;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Date startDate;

    @Column(nullable = false)
    private Date endDate;

    @Column
    private Long mapX;

    @Column
    private Long mapY;

    @Column
    private int areaCode;

    @Column(nullable = false)
    private int likeCount = 0;

}
