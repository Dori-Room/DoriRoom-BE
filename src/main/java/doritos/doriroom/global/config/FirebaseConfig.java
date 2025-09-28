package doritos.doriroom.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FirebaseConfig {
    @Value("${firebase.project_id}")
    private String projectId;

    @Value("${firebase.client_email}")
    private String clientEmail;

    @Value("${firebase.client_id}")
    private String clientId;

    @Value("${firebase.private_key_id}")
    private String privateKeyId;

    @Value("${firebase.private_key}")
    private String privateKey;

    @PostConstruct
    public void init() throws IOException {
        String formattedKey = privateKey.replace("\n", "\\n");

        String json = String.format("""
        {
          "type": "service_account",
          "project_id": "%s",
          "private_key_id": "%s",
          "private_key": "%s",
          "client_email": "%s",
          "client_id": "%s",
          "token_uri": "https://oauth2.googleapis.com/token"
        }
        """, projectId, privateKeyId, formattedKey, clientEmail, clientId);

        try (ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }
    }
}
