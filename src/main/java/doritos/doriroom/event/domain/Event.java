package doritos.doriroom.event.domain;

import doritos.doriroom.event.exception.EventNotFoundException;
import doritos.doriroom.tourApi.dto.response.TourApiDetailInfoDto;
import doritos.doriroom.tourApi.dto.response.TourApiDetailIntroDto;
import doritos.doriroom.tourApi.dto.response.TourApiItemDto;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private int sigungucode;

    @Column
    private String cat1;

    @Column
    private String cat2;

    @Column
    private String cat3;

    @Column
    private String lclsSystm1;

    @Column
    private String lclsSystm2;

    @Column
    private String lclsSystm3;

    @Column
    private String sponsor1; //주최사

    @Column
    private String sponsor2; //주관사

    @Column
    private String useTimeFestival; //가격

    @Column(columnDefinition = "TEXT")
    private String eventIntro;

    @Column(columnDefinition = "TEXT")
    private String eventContent;

    @Column(nullable = false)
    private int likeCount = 0;

    public static Event fromEntity(TourApiItemDto dto){
        if (dto.getContentid() == null || dto.getContentid().isBlank()) {
            throw new EventNotFoundException();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return Event.builder()
            .eventId(UUID.randomUUID())
            .contentId(parseInt(dto.getContentid()))
            .contentTypeId(parseInt(dto.getContenttypeid()))
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
            .sigungucode(parseInt(dto.getSigungucode()))
            .cat1(dto.getCat1())
            .cat2(dto.getCat2())
            .cat3(dto.getCat3())
            .lclsSystm1(dto.getLclsSystm1())
            .lclsSystm2(dto.getLclsSystm2())
            .lclsSystm3(dto.getLclsSystm3())
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
        this.sigungucode = newEvent.sigungucode;
        this.contentTypeId = newEvent.contentTypeId;
        this.cat1 = newEvent.cat1;
        this.cat2 = newEvent.cat2;
        this.cat3 = newEvent.cat3;
        this.lclsSystm1 = newEvent.lclsSystm1;
        this.lclsSystm2 = newEvent.lclsSystm2;
        this.lclsSystm3 = newEvent.lclsSystm3;
    }

    public void updateDetailFrom(TourApiDetailIntroDto dto){
        if(dto != null){
            this.sponsor1 = dto.getSponsor1();
            this.sponsor2 = dto.getSponsor2();
            this.useTimeFestival = dto.getUsetimefestival();
        }
    }

    public void updateDetailInfoFrom(List<TourApiDetailInfoDto> dtoList){
        if(dtoList != null){
            for(TourApiDetailInfoDto detailInfo : dtoList){
                if("행사소개".equals(detailInfo.getInfoname())){
                    this.eventIntro = detailInfo.getInfotext();
                } else if("행사내용".equals(detailInfo.getInfoname())){
                    this.eventContent = detailInfo.getInfotext();
                }
            }
        }
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
