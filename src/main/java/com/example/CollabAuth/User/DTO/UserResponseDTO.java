package com.example.CollabAuth.User.DTO;

import com.example.CollabAuth.User.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String provider;
    private String token;
    private LocalDateTime createdAt;
}
