package com.example.CollabAuth.OAuth;

import com.example.CollabAuth.User.User;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserPrinciple implements OidcUser {
    private UUID id;
    private String username;
    private String email;
    private User.AuthProvider provider;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;
    private OidcIdToken oidcToken;
    private OidcUserInfo oidcUserInfo;


    public static UserPrinciple loginUser(User user){
        Collection<GrantedAuthority> authorityCollections = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        return UserPrinciple.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .provider(user.getProvider())
                .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : Collections.emptyList();
    }

    @Override
    public String getName() {
        return username; // or email, depending on your preference
    }

    @Override
    public Map<String, Object> getClaims() {
        return Collections.emptyMap(); // Return empty map or implement as needed
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUserInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcToken;
    }
}
