package doritos.doriroom.global.jwt;

import doritos.doriroom.global.exception.ApiException;
import doritos.doriroom.user.domain.RefreshToken;
import doritos.doriroom.user.exception.UsernameNotFoundException;
import doritos.doriroom.user.repository.RefreshTokenRedisRepository;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.repository.UserRepository;
import io.jsonwebtoken.security.Keys;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final UserRepository userRepository;

    private Key getSignKey(){
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        long exp = jwtProperties.getAccessTokenExpirationMinutes() * 60 * 1000L;

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + exp))
                .signWith(SignatureAlgorithm.HS512, getSignKey())
                .compact();
    }

    public String generateRefresh(User user) {
        long exp = jwtProperties.getRefreshTokenExpirationDays() * 24 * 60 * 60 * 1000L;
        long expSeconds = jwtProperties.getRefreshTokenExpirationDays() * 24 * 60 * 60L;

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + exp))
                .signWith(SignatureAlgorithm.HS512, getSignKey())
                .compact();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getUserId())
                .refreshToken(token)
                .ttl(expSeconds)
                .build();

        refreshTokenRedisRepository.save(refreshToken);

        return token;
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank())  return false;

        try{
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public User getUserFromToken(String token) {
        try{
            String username = getUsernameFromToken(token);
            if(username == null)    return null;

            return userRepository.findByUsername(username)
                    .orElseThrow(UsernameNotFoundException::new);

        } catch (Exception e){
            return null;
        }
    }
}
