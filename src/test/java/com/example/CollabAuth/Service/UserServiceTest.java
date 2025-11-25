package com.example.CollabAuth.Service;



import com.example.CollabAuth.Configuration.AuthConfig;
import com.example.CollabAuth.Email.EmailClientService;
import com.example.CollabAuth.ErrorHandler.GlobalExceptionHandler.*;
import com.example.CollabAuth.OAuth.UserPrinciple;
import com.example.CollabAuth.User.DTO.LoginRequestDTO;
import com.example.CollabAuth.User.DTO.RegisterRequestDTO;
import com.example.CollabAuth.User.DTO.UserResponseDTO;
import com.example.CollabAuth.User.Imp.ServiceImp;
import com.example.CollabAuth.User.Security.JwtUtils;
import com.example.CollabAuth.User.User;
import com.example.CollabAuth.User.UserMapper;
import com.example.CollabAuth.User.UserRepo;
import com.example.CollabAuth.User.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceImpTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepo userRepo;

    @Mock
    private AuthConfig authConfig;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private EmailClientService emailClientService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ServiceImp serviceImp;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private User testUser;
    private UserResponseDTO userResponse;
    private UserPrinciple userPrinciple;

    @BeforeEach
    void setUp() {
        // Setup common test data
        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .provider(User.AuthProvider.LOCAL)
                .providerId("None")
                .createdAt(LocalDateTime.now())
                .build();

        userResponse = new UserResponseDTO();
        userResponse.setId(testUser.getId());
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");

        userPrinciple = UserPrinciple.loginUser(testUser);

        // Mock AuthConfig to return passwordEncoder
//        when(authConfig.passwordEncoder()).thenReturn(passwordEncoder);
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepo.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(userResponse);
        doNothing().when(emailClientService).SendWelcomeMail(anyString(), anyString());

        // Act
        UserResponseDTO result = serviceImp.registerUser(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepo, times(1)).existsByUsername("testuser");
        verify(userRepo, times(1)).existsByEmail("test@example.com");
        verify(userRepo, times(1)).save(any(User.class));
        verify(emailClientService, times(1)).SendWelcomeMail("test@example.com", "testuser");
    }

    @Test
    void testRegisterUser_UsernameAlreadyExists_ThrowsDuplicateResourceException() {
        // Arrange
        when(userRepo.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> serviceImp.registerUser(registerRequest)
        );

        assertEquals("Username Already Exists", exception.getMessage());
        verify(userRepo, times(1)).existsByUsername("testuser");
        verify(userRepo, never()).save(any(User.class));
        verify(emailClientService, never()).SendWelcomeMail(anyString(), anyString());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists_ThrowsDuplicateResourceException() {
        // Arrange
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> serviceImp.registerUser(registerRequest)
        );

        assertEquals("Email Already Exists", exception.getMessage());
        verify(userRepo, times(1)).existsByUsername("testuser");
        verify(userRepo, times(1)).existsByEmail("test@example.com");
        verify(userRepo, never()).save(any(User.class));
        verify(emailClientService, never()).SendWelcomeMail(anyString(), anyString());
    }

    @Test
    void testLoginUser_InvalidPassword_ThrowsValidationException() {
        // Arrange
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> serviceImp.loginUser(loginRequest)
        );

        assertEquals("Invalid credentials or User Does NOt found", exception.getMessage());
        verify(userRepo, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "hashedPassword");
        verify(jwtUtils, never()).generateTokenFromUserPrinciple(any(UserPrinciple.class));
    }

    @Test
    void testCurrentUser_Success() {
        // Arrange
        UUID userId = testUser.getId();
        when(userRepo.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(userResponse);

        // Act
        UserResponseDTO result = serviceImp.currentUser(userPrinciple);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepo, times(1)).findById(userId);
        verify(userMapper, times(1)).toUserResponseDTO(any(User.class));
    }

    @Test
    void testCurrentUser_UserNotFound_ThrowsValidationException() {
        // Arrange
        UUID userId = userPrinciple.getId();
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> serviceImp.currentUser(userPrinciple)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepo, times(1)).findById(userId);
        verify(userMapper, never()).toUserResponseDTO(any(User.class));
    }

    @Test
    void testRegisterUser_PasswordIsHashed() {
        // Arrange
        String rawPassword = "password123";
        String hashedPassword = "hashedPassword";

        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(hashedPassword, user.getPassword());
            return user;
        });
        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(userResponse);
        doNothing().when(emailClientService).SendWelcomeMail(anyString(), anyString());

        // Act
        serviceImp.registerUser(registerRequest);

        // Assert
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    void testRegisterUser_SetsCorrectProviderAndProviderId() {
        // Arrange
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(User.AuthProvider.LOCAL, user.getProvider());
            assertEquals("None", user.getProviderId());
            assertNotNull(user.getCreatedAt());
            return user;
        });
        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(userResponse);
        doNothing().when(emailClientService).SendWelcomeMail(anyString(), anyString());

        // Act
        serviceImp.registerUser(registerRequest);

        // Assert
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void testLoginUser_GeneratesTokenWithCorrectUserPrinciple() {
        // Arrange
        String jwtToken = "jwt-token";

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.generateTokenFromUserPrinciple(any(UserPrinciple.class))).thenReturn(jwtToken);
        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(userResponse);

        // Act
        serviceImp.loginUser(loginRequest);

        // Assert
        verify(jwtUtils, times(1)).generateTokenFromUserPrinciple(argThat(principle ->
                principle.getId().equals(testUser.getId()) &&
                        principle.getUsername().equals(testUser.getUsername()) &&
                        principle.getEmail().equals(testUser.getEmail())
        ));
    }

}