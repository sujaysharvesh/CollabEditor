package com.example.CollabAuth.User;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;
    private String password;
    private String email;
    private AuthProvider provider;
    private String providerId;
    private LocalDateTime createdAt;

    public enum AuthProvider {
        LOCAL,
        GOOGLE,
        FACEBOOK,
        GITHUB
    }
}
