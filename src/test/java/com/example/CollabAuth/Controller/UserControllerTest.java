package com.example.CollabAuth.Controller;

import com.example.CollabAuth.ErrorHandler.GlobalExceptionHandler.*;
import com.example.CollabAuth.OAuth.UserPrinciple;
import com.example.CollabAuth.User.DTO.LoginRequestDTO;
import com.example.CollabAuth.User.DTO.RegisterRequestDTO;
import com.example.CollabAuth.User.DTO.UserResponseDTO;
import com.example.CollabAuth.User.Security.CookieBuilder;
import com.example.CollabAuth.User.UserController;
import com.example.CollabAuth.User.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private CookieBuilder cookieBuilder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testHome_ShouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/api/v1/auth/home"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to the User Authentication Service!"));
    }

    @Test
    void testGetCsrfToken_ShouldReturnCsrfToken() throws Exception {
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getHeaderName()).thenReturn("X-CSRF-TOKEN");
        when(csrfToken.getToken()).thenReturn("test-csrf-token");

        mockMvc.perform(get("/api/v1/auth/csrf")
                        .requestAttr(CsrfToken.class.getName(), csrfToken))
                .andExpect(status().isOk())
                .andExpect(header().string("X-CSRF-TOKEN", "test-csrf-token"))
                .andExpect(jsonPath("$.Token").exists());
    }

    @Test
    void testCurrentUser_WhenAuthenticated_ShouldReturnUser() throws Exception {
        UserPrinciple userPrinciple = mock(UserPrinciple.class);
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(UUID.fromString("testUser"));
        userResponse.setEmail("test@example.com");
        userResponse.setUsername("testuser");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrinciple);
        when(userService.currentUser(userPrinciple)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Current user retrieved successfully"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testCurrentUser_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        when(securityContext.getAuthentication()).thenReturn(null);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }
}