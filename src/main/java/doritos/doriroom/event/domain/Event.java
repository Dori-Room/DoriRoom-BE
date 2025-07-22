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
    private int contentId;

    @Column(nullable = false)
    private int contentTypeId;

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
    private String addr1;

    @Column
    private String addr2;

    @Column
    private Double mapX;

    @Column
    private Double mapY;

    @Column
    private int areaCode;

    @Column
    private String cat1;

    @Column
    private String cat2;

    @Column
    private String cat3;

    @Column(length = 500)
    private String tel;

    @Column(nullable = false)
    private int likeCount = 0;

    public static Event fromEntity(EventApiItemDto dto){
        if (dto.getContentid() == null || dto.getContentid().isBlank()) {
            throw new IllegalArgumentException("contentId 없음: " + dto.getTitle());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return Event.builder()
            .eventId(UUID.randomUUID())
            .contentId(parseInt(dto.getContentid()))
            .contentTypeId(parseInt(dto.getContentid()))
            .firstImage(dto.getFirstimage())
            .secondImage(dto.getFirstimage2())
            .title(dto.getTitle())
            .startDate(LocalDate.parse(dto.getEventstartdate(), formatter))
            .endDate(LocalDate.parse(dto.getEventenddate(), formatter))
            .addr1(dto.getAddr1())
            .addr2(dto.getAddr2())
            .mapX(parseDouble(dto.getMapx()))
            .mapY(parseDouble(dto.getMapy()))
            .areaCode(parseInt(dto.getAreacode()))
            .cat1(dto.getCat1())
            .cat2(dto.getCat2())
            .cat3(dto.getCat3())
            .tel(dto.getTel())
            .likeCount(0)
            .build();
    }

    public void updateFrom(Event newEvent) {
        this.firstImage = newEvent.firstImage;
        this.secondImage = newEvent.secondImage;
        this.title = newEvent.title;
        this.startDate = newEvent.startDate;
        this.endDate = newEvent.endDate;
        this.addr1 = newEvent.addr1;
        this.addr2 = newEvent.addr2;
        this.mapX = newEvent.mapX;
        this.mapY = newEvent.mapY;
        this.areaCode = newEvent.areaCode;
        this.contentTypeId = newEvent.contentTypeId;
        this.cat1 = newEvent.cat1;
        this.cat2 = newEvent.cat2;
        this.cat3 = newEvent.cat3;
        this.tel = newEvent.getTel();
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
