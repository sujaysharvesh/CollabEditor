package com.example.CollabAuth.User.Security;


import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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


    public Cookie createJwtCookie(String token) {
        Cookie cookie = new Cookie(jwtCookieName,token);
        cookie.setHttpOnly(true);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(cookieMaxAge);

        if(!cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain);
        }
        return cookie;
    }

    public Cookie createLogoutCookie() {
        Cookie cookie = new Cookie(jwtCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(isProduction());
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0); // Expire immediately

        if (!cookieDomain.isEmpty()) {
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

        if (!cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain);
        }

        return cookie;
    }

    private boolean isProduction() {
        return "production".equalsIgnoreCase(environment);
    }


}
