package com.example.CollabAuth.User.Imp;

import com.example.CollabAuth.ErrorHandler.GlobalExceptionHandler.*;
import com.example.CollabAuth.User.DTO.RegisterRequestDTO;
import com.example.CollabAuth.User.DTO.UserResponseDTO;
import com.example.CollabAuth.User.User;
import com.example.CollabAuth.User.UserMapper;
import com.example.CollabAuth.User.UserRepo;
import com.example.CollabAuth.User.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class serviceImp implements UserService {

    private final UserRepo userRepo;

    @Autowired
    public UserMapper userMapper;


    @Override
    public UserResponseDTO registerUser(RegisterRequestDTO request) {
        validateUserRegistration(request);
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .provider(User.AuthProvider.LOCAL)
                .providerId("None")
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepo.save(user);
        return userMapper.toUserResponseDTO(savedUser);
    }

    private void validateUserRegistration(RegisterRequestDTO request) {
        if (userRepo.existsByUsername(request.getUsername())){
            throw new DuplicateResourceException("Username Already Exists");
        }
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email Already Exists");
        }
    }
}
