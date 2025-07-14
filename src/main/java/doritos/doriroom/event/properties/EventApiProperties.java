package doritos.doriroom.event.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tour-api")
@Getter
@Setter
public class EventApiProperties {
    private Event event;

    @Getter
    @Setter
    public static class Event {
        private String url;
        private String key;
    }
}
