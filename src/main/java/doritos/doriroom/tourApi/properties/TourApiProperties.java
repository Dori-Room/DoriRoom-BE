package doritos.doriroom.tourApi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tour-api")
@Getter
@Setter
public class TourApiProperties {
    private Event event;
    private Sigungu sigungu;

    @Getter
    @Setter
    public static class Event {
        private String url;
        private String key;
    }

    @Getter
    @Setter
    public static class Sigungu {
        private String url;
        private String key;
    }
}
