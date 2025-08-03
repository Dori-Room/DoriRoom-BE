package doritos.doriroom.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.global.exception.ApiException;
import doritos.doriroom.user.domain.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

   @Override
    public void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        try {
            if (token != null) {
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
            errorResponse(response, new ApiException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 입니다."));
            return;
        }

        filterChain.doFilter(request, response);
    }


    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void errorResponse(HttpServletResponse response, ApiException e) throws IOException {
        response.setStatus(e.getHttpStatus().value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(e.getHttpStatus(), e.getMessage())));
    }

}
