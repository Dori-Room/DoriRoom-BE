package doritos.doriroom.event.domain;

import doritos.doriroom.event.dto.response.EventApiItemDto;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "event")
@Getter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Event {
    @Id
    private UUID eventId;

    @Column(nullable = false, unique = true)
    private String contentId;

    @Column
    private String firstImage;

    @Column
    private String secondImage;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column
    private Double mapX;

    @Column
    private Double mapY;

    @Column
    private int areaCode;

    @Column(nullable = false)
    private int likeCount = 0;

    public static Event fromEntity(EventApiItemDto dto){
        if (dto.getContentid() == null || dto.getContentid().isBlank()) {
            throw new IllegalArgumentException("contentId 없음: " + dto.getTitle());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return Event.builder()
            .eventId(UUID.randomUUID())
            .contentId(dto.getContentid())
            .firstImage(dto.getFirstimage())
            .secondImage(dto.getFirstimage2())
            .title(dto.getTitle())
            .startDate(LocalDate.parse(dto.getEventstartdate(), formatter))
            .endDate(LocalDate.parse(dto.getEventenddate(), formatter))
            .mapX(parseDouble(dto.getMapx()))
            .mapY(parseDouble(dto.getMapy()))
            .areaCode(parseInt(dto.getAreacode()))
            .likeCount(0)
            .build();
    }

    private static double parseDouble(String value) {
        try {
            return (value != null && !value.isBlank())
                ? Double.parseDouble(value)
                : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static int parseInt(String value) {
        try {
            return (value != null && !value.isBlank())
                ? Integer.parseInt(value)
                : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
