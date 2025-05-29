package com.example.CollabAuth.User;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;
    private String password;
    private String email;
    private AuthProvider provider;
    private LocalDateTime createdAt;

    public enum AuthProvider {
        LOCAL,
        GOOGLE,
        FACEBOOK,
        GITHUB
    }
}
