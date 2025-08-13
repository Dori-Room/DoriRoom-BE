package doritos.doriroom.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import doritos.doriroom.auth.exception.InvalidTokenException;
import doritos.doriroom.auth.exception.TokenExpiredException;
import doritos.doriroom.auth.service.AuthService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.global.exception.ApiException;
import doritos.doriroom.user.domain.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

   @Override
    public void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtUtil.extractToken(request);

        try {
            if (token != null) {
                String blacklistedKey = "blacklist_token:" + token;
                if (redisTemplate.hasKey(blacklistedKey)) {
                    throw new InvalidTokenException("로그아웃된 토큰입니다.");
                }

                jwtUtil.validateToken(token); // 토큰 유효성 검사

                User user = jwtUtil.getUserFromToken(token);

                // SecurityContext 설정
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, null);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (TokenExpiredException e) {
            errorResponse(response, e);
            return;
        } catch (JwtException e) {
            errorResponse(response, new InvalidTokenException("유효하지 않은 토큰 입니다."));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void errorResponse(HttpServletResponse response, ApiException e) throws IOException {
        response.setStatus(e.getHttpStatus().value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(e.getHttpStatus(), e.getMessage())));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/api/auth/reissue");
    }

}
