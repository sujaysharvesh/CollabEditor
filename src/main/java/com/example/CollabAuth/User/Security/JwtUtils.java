package com.example.CollabAuth.User.Security;

import com.example.CollabAuth.OAuth.UserPrinciple;
import com.example.CollabAuth.User.User;
import com.example.CollabAuth.User.UserRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Autowired
    private UserRepo userRepo;


    @Value("${jwt.secret}")
    private String SECRET_KEY_STRING;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    }


    public String generateToken(Authentication authentication) {
        UserPrinciple userPrinciple1 = (UserPrinciple) authentication.getPrincipal();
        return generateTokenFromUserPrinciple(userPrinciple1);
    }

    public String generateTokenFromUserPrinciple(UserPrinciple userPrinciple) {
        Instant now  = Instant.now();
        List<String> roles = userPrinciple.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return Jwts.builder()
                .setSubject(userPrinciple.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .claim("email", userPrinciple.getEmail())
                .claim("username", userPrinciple.getUsername())
                .claim("roles", roles)
                .claim("auth_provider", userPrinciple.getProvider())
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).build().parseSignedClaims(token).getBody();
        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found" ));
        Collection<? extends GrantedAuthority> authorities = ((List<String>) claims.get("roles"))
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        UserPrinciple userPrinciple = UserPrinciple.createByClaims(claims);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userPrinciple, token, userPrinciple.getAuthorities());
        return usernamePasswordAuthenticationToken;
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }


}
