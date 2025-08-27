package doritos.doriroom.atlas.policy;

import org.springframework.stereotype.Component;

@Component
public class LevelPolicy {

    public Long calculateRequiredExp(int currentLevel) {
        // 임의로 레벨별 필요 경험치를 레벨 * 100 exp로 지정함
        if (currentLevel == 0)  return (long) 100;

        return (long) currentLevel * 100 ;
    }

}
