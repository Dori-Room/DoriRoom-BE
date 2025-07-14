package doritos.doriroom.global.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory("https://apis.data.go.kr");
        uriBuilderFactory.setEncodingMode(EncodingMode.NONE);

        return WebClient.builder()
            .uriBuilderFactory(uriBuilderFactory)
            .codecs(configurer ->
                configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024) // 5MB
            )
            .baseUrl("https://apis.data.go.kr")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
