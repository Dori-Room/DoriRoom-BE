package doritos.doriroom.user.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Valid
public record SpeechBubbleRequest(
        @NotBlank @Size(max = 30)
        String speechBubble
){}
