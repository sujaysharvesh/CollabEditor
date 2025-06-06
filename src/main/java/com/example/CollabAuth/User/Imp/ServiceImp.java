package com.example.CollabAuth.User.Imp;

import com.example.CollabAuth.Configuration.AuthConfig;
import com.example.CollabAuth.ErrorHandler.GlobalExceptionHandler.*;
import com.example.CollabAuth.OAuth.UserPrinciple;
import com.example.CollabAuth.User.DTO.LoginRequestDTO;
import com.example.CollabAuth.User.DTO.RegisterRequestDTO;
import com.example.CollabAuth.User.DTO.UserResponseDTO;
import com.example.CollabAuth.User.Security.JwtUtils;
import com.example.CollabAuth.User.User;
import com.example.CollabAuth.User.UserMapper;
import com.example.CollabAuth.User.UserRepo;
import com.example.CollabAuth.User.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ServiceImp implements UserService {

    private final UserMapper userMapper;
    private final UserRepo userRepo;
    private final AuthConfig authConfig;
    private final JwtUtils jwtUtils;



    @Override
    public UserResponseDTO registerUser(RegisterRequestDTO request) {
        validateUserRegistration(request);
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(hashPassword(request.getPassword()))
                .provider(User.AuthProvider.LOCAL)
                .providerId("None")
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepo.save(user);
        return userMapper.toUserResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO loginUser(LoginRequestDTO request) {
        User user = verifyUser(request);
        UserPrinciple userPrinciple = UserPrinciple.loginUser(user);
        String token = jwtUtils.generateTokenFromUserPrinciple(userPrinciple);
        UserResponseDTO response = userMapper.toUserResponseDTO(user);
        response.setToken(token);
        return response;
    }

    @Override
    public UserResponseDTO currentUser(UserPrinciple userPrinciple) {
        User currentUser = getCurrentUser(userPrinciple.getId());
        User user = User.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .build();
        UserResponseDTO  response = userMapper.toUserResponseDTO(user);
        response.setId(currentUser.getId());
        return response;
    }

    private void validateUserRegistration(RegisterRequestDTO request) {
        if (userRepo.existsByUsername(request.getUsername())){
            throw new DuplicateResourceException("Username Already Exists");
        }
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email Already Exists");
        }
    }

    private String hashPassword(String password) {
        return authConfig.passwordEncoder().encode(password);
    }

    private User getCurrentUser(UUID userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found"));
    }

    private User verifyUser(LoginRequestDTO request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("Invalid Credentials or User Does Not Exist"));
        if (!authConfig.passwordEncoder().matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException("Invalid credentials or User Does NOt found");
        }
        return user;
        }
}
