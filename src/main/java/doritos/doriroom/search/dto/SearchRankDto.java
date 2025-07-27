package doritos.doriroom.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchRankDto {
    private String keyword;
    private Long count;
    private Integer rank;
    private RankChange change;
}