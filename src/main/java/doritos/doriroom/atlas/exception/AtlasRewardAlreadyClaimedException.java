package doritos.doriroom.atlas.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AtlasRewardAlreadyClaimedException extends ApiException {
    public AtlasRewardAlreadyClaimedException() {
        super(HttpStatus.CONFLICT, "이미 수령한 보상입니다.");
    }
}
