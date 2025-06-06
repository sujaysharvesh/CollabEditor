package com.example.CollabAuth.User;


import com.example.CollabAuth.ErrorHandler.GlobalExceptionHandler.*;
import com.example.CollabAuth.OAuth.UserPrinciple;
import com.example.CollabAuth.User.DTO.LoginRequestDTO;
import com.example.CollabAuth.User.DTO.RegisterRequestDTO;
import com.example.CollabAuth.User.DTO.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/api/v1/Auth")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String home() {
        return "Welcome to the User Authentication Service!";
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> currentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User not authenticated"));
            }
            UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
            UserResponseDTO userResponse = userService.currentUser(userPrinciple);
            ApiResponse<UserResponseDTO> response = ApiResponse
                    .success("Current user retrieved successfully", userResponse);
            return ResponseEntity.ok(response);
        } catch (ValidationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ex.getMessage()));
        }


    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            UserResponseDTO newUser = userService.registerUser(request);
            ApiResponse<UserResponseDTO> response = ApiResponse
                    .success("User registered successfully", newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DuplicateResourceException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
        } catch (ValidationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            UserResponseDTO user = userService.loginUser(request);
            ApiResponse<UserResponseDTO> response = ApiResponse
                    .success("User logged in successfully", user);
            return ResponseEntity.ok(response);
        } catch (ValidationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ex.getMessage()));
        }
    }
}
