package com.example.CollabAuth.User;


import com.example.CollabAuth.ErrorHandler.GlobalExceptionHandler.*;
import com.example.CollabAuth.OAuth.UserPrinciple;
import com.example.CollabAuth.User.DTO.LoginRequestDTO;
import com.example.CollabAuth.User.DTO.RegisterRequestDTO;
import com.example.CollabAuth.User.DTO.UserResponseDTO;
import com.example.CollabAuth.User.Security.CookieBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private UserService userService;
    private CookieBuilder cookieBuilder;

    @Autowired
    public UserController(UserService userService, CookieBuilder cookieBuilder) {
        this.userService = userService;
        this.cookieBuilder = cookieBuilder;
    }

    @GetMapping("/home")
    public String home() {
        return "Welcome to the User Authentication Service!";
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, Object>>getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("Error", "CSRF TOKEN NOT FOUND"));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .header(csrfToken.getHeaderName(), csrfToken.getToken())
                .body(Map.of("Token", csrfToken));
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
    public ResponseEntity<ApiResponse<UserResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request,
                                                              HttpServletRequest httpRequest,
                                                              HttpServletResponse response) {
        try {
            UserResponseDTO user = userService.loginUser(request);
            cookieBuilder.setJwtCookie(response, user.getToken());
            user.setToken(null);
            ApiResponse<UserResponseDTO> apiResponse = ApiResponse
                    .success("User logged in successfully", user);

            return ResponseEntity.ok(apiResponse);

        } catch (ValidationException ex) {
            log.error("Login validation failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Login failed with unexpected error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred during login"));
        }
    }
}
