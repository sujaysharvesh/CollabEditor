package com.example.CollabAuth.User.Security;


import com.example.CollabAuth.User.Redis.RedisBlockListService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisBlockListService redisBlockListService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);
            if(!StringUtils.hasText(token)) {
                filterChain.doFilter(request, response);
                return;
            }
            if(redisBlockListService.isBlocked(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access");
                return;
            }
            if(jwtUtils.isValidToken(token)) {
                Authentication authentication = jwtUtils.getAuthentication(token);
                log.info("JWT authentication successful for user: {}", authentication.getName());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
            return;
        } catch (JwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failed");
            return;
        }
        filterChain.doFilter(request, response);

    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if(cookies!=null) {
            for(Cookie cookie : cookies) {
                if("jwt-token".equals(cookie.getName())) {
                    log.info("JWT token extracted from cookie: {}", cookie.getName());
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

}
