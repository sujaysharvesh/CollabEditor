package com.example.CollabAuth.User.Imp;

import com.example.CollabAuth.User.DTO.RegisterRequestDTO;
import com.example.CollabAuth.User.DTO.UserResponseDTO;
import com.example.CollabAuth.User.UserRepo;
import com.example.CollabAuth.User.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class serviceImp implements UserService {

    private final UserRepo userRepo;

    @Override
    public UserResponseDTO registerUser(RegisterRequestDTO registerRequestDTO) {
        return null;
    }
}
