package com.example.CollabAuth.User.Security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CookieBuilder {

    @Value("${app.jwt.cookie-name:jwt-token}")
    private String jwtCookieName;

    @Value("${app.jwt.cookie-path:/}")
    private String cookiePath;

    @Value("${app.jwt.cookie-domain:}")
    private String cookieDomain;

    @Value("${app.jwt.cookie-max-age:86400}")
    private int cookieMaxAge;

    @Value("${app.environment:development}")
    private String environment;

    public void setJwtCookie(HttpServletResponse response, String token) {
        log.info("Setting JWT cookie with token: {}", token != null ? ("present, length: " + token.length()) : "null");

        boolean isProduction = isProduction();
        boolean isSecure = isProduction;

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(jwtCookieName, token)
                .httpOnly(true)
                .secure(isSecure)
                .path(cookiePath)
                .maxAge(cookieMaxAge)
                .sameSite("Lax");

        // Only set domain if specified and not localhost
        if (!cookieDomain.isEmpty() && !cookieDomain.contains("localhost")) {
            cookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

    }

    public Cookie createLogoutCookie() {
        Cookie cookie = new Cookie(jwtCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(isProduction());
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0); // Expire immediately

        // Only set domain if specified and not localhost
        if (!cookieDomain.isEmpty() && !cookieDomain.contains("localhost")) {
            cookie.setDomain(cookieDomain);
        }
        return cookie;
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh-token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(isProduction());
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge(604800); // 7 days

        // Only set domain if specified and not localhost
        if (!cookieDomain.isEmpty() && !cookieDomain.contains("localhost")) {
            cookie.setDomain(cookieDomain);
        }

        return cookie;
    }

    private boolean isProduction() {
        return "production".equalsIgnoreCase(environment);
    }
}