package com.example.CollabAuth.User;

import com.example.CollabAuth.User.DTO.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    UserResponseDTO toUserResponseDTO(User user);
}
