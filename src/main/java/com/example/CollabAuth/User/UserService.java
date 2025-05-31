package com.example.CollabAuth.User;

import com.example.CollabAuth.User.DTO.LoginRequestDTO;
import com.example.CollabAuth.User.DTO.RegisterRequestDTO;
import com.example.CollabAuth.User.DTO.UserResponseDTO;
import org.springframework.stereotype.Service;


@Service
public interface UserService {

    UserResponseDTO registerUser(RegisterRequestDTO registerRequestDTO);
    UserResponseDTO loginUser(LoginRequestDTO request);

}
