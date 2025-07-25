package doritos.doriroom.tourApi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "area")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area {
    @Id
    private Integer code;
    
    @Column(nullable = false)
    private String name;
} 