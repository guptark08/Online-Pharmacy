package com.pharmacy.auth.service;

import com.pharmacy.auth.dto.AuthResponse;
import com.pharmacy.auth.dto.LoginRequest;
import com.pharmacy.auth.dto.SignupRequest;
import com.pharmacy.auth.dto.UserProfileDTO;
import com.pharmacy.auth.entity.User;
import com.pharmacy.auth.enums.Role;
import com.pharmacy.auth.repository.UserRepository;
import com.pharmacy.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("john@example.com");
        signupRequest.setMobile("1234567890");
        signupRequest.setPassword("password123");
        signupRequest.setAddress("123 Main St");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setMobile("1234567890");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.CUSTOMER);
        testUser.setAddress("123 Main St");
        testUser.setActive(true);
    }

    @Test
    void signup_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByMobile(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.signup(signupRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.CUSTOMER, response.getRole());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_EmailAlreadyRegistered() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        SignupRequest request = new SignupRequest();
        request.setEmail("existing@example.com");

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.signup(request));

        assertTrue(exception.getMessage().contains("Email already registered"));
    }

    @Test
    void signup_MobileAlreadyRegistered() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByMobile(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.signup(signupRequest));

        assertTrue(exception.getMessage().contains("Mobile already registered"));
    }

    @Test
    void login_Success() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("user", "pass"));
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        lenient().when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        lenient().when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("John Doe", response.getName());
    }

    @Test
    void login_UserNotFound() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("user", "pass"));
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void login_AccountDeactivated() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("user", "pass"));
        testUser.setActive(false);
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));

        assertTrue(exception.getMessage().contains("deactivated"));
    }

    @Test
    void getProfile_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserProfileDTO profile = authService.getProfile("john@example.com");

        assertNotNull(profile);
        assertEquals(1L, profile.getId());
        assertEquals("John Doe", profile.getName());
        assertEquals("john@example.com", profile.getEmail());
    }

    @Test
    void getProfile_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.getProfile("unknown@example.com"));

        assertTrue(exception.getMessage().contains("User not found"));
    }
}
