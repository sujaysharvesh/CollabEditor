package com.example.CollabAuth.User;

import com.example.CollabAuth.User.DTO.UserResponseDTO;
import org.hibernate.annotations.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
