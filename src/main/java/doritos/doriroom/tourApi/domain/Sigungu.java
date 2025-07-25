package doritos.doriroom.tourApi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sigungu")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(SigunguId.class)
public class Sigungu {
    @Id
    private Integer code;

    @Id
    private Integer areaCode;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String areaName;
} 