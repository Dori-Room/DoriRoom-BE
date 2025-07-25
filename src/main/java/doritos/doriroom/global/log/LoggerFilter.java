package doritos.doriroom.global.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
public class LoggerFilter extends OncePerRequestFilter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void doFilterInternal(
        @Nonnull HttpServletRequest request,
        @Nonnull HttpServletResponse response,
        @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis();

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        wrappedResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());

        filterChain.doFilter(request, wrappedResponse);

        long end = System.currentTimeMillis();
        long elapsed = end - start;

        ResponseInfo responseInfo = getResponseInfo(wrappedResponse);

        log.info("{} {} {} {}ms {}{}",
            request.getMethod(),
            request.getRequestURI(),
            getIp(request),
            elapsed,
            responseInfo.statusCode(),
            responseInfo.error() == null ? "" : " - %s".formatted(responseInfo.error())
        );

        wrappedResponse.copyBodyToResponse(); // 응답을 원본에 복사
    }

    private ResponseInfo getResponseInfo(ContentCachingResponseWrapper response) {
        try {
            String responseBody = new String(response.getContentAsByteArray(), response.getCharacterEncoding());
            JsonNode jsonNode = OBJECT_MAPPER.readTree(responseBody);

            int statusCode = jsonNode.has("statusCode") ? jsonNode.get("statusCode").asInt() : response.getStatus();
            String error = jsonNode.has("error") && !jsonNode.get("error").isNull() ? jsonNode.get("error").asText() : null;

            return new ResponseInfo(statusCode, error);
        } catch (Exception e) {
            log.warn("Failed to parse response body", e);
            return new ResponseInfo(response.getStatus(), null);
        }
    }

    private String getIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader == null) ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return isSwaggerRequest(request) || isActuatorRequest(request);
    }

    private boolean isSwaggerRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger-ui/") || uri.startsWith("/v3/api-docs");
    }

    private boolean isActuatorRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/actuator/");
    }

    private record ResponseInfo(int statusCode, String error) {}
}
