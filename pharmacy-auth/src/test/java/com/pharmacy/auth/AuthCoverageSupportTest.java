package com.pharmacy.auth;

import com.pharmacy.auth.controller.AdminUserController;
import com.pharmacy.auth.controller.AuthController;
import com.pharmacy.auth.dto.AuthResponse;
import com.pharmacy.auth.dto.LoginRequest;
import com.pharmacy.auth.dto.SignupRequest;
import com.pharmacy.auth.dto.UserProfileDTO;
import com.pharmacy.auth.dto.UserUpdateRequest;
import com.pharmacy.auth.entity.User;
import com.pharmacy.auth.enums.Role;
import com.pharmacy.auth.exception.GlobalExceptionHandler;
import com.pharmacy.auth.repository.UserRepository;
import com.pharmacy.auth.security.JwtAuthFilter;
import com.pharmacy.auth.security.JwtUtil;
import com.pharmacy.auth.service.AuthService;
import com.pharmacy.auth.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthCoverageSupportTest {

    private static final String SECRET =
        "cGhhcm1hY3ktc3VwZXItc2VjcmV0LWtleS0yMDI0LXNwcmluZy1ib290LWp3dA==";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void main_ShouldDelegateToSpringApplication() {
        String[] args = {"--test"};

        try (MockedStatic<SpringApplication> springApplication =
                     mockStatic(SpringApplication.class)) {
            PharmacyAuthApplication.main(args);

            springApplication.verify(() ->
                SpringApplication.run(PharmacyAuthApplication.class, args));
        }
    }

    @Test
    void authController_ShouldDelegateToService() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", authService);

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("signup@pharmacy.com");
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@pharmacy.com");
        loginRequest.setPassword("secret");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("profile@pharmacy.com")
            .password("password")
            .authorities("ROLE_CUSTOMER")
            .build();

        AuthResponse signupResponse = new AuthResponse();
        signupResponse.setEmail("signup@pharmacy.com");
        AuthResponse loginResponse = new AuthResponse();
        loginResponse.setEmail("login@pharmacy.com");
        UserProfileDTO profile = new UserProfileDTO();
        profile.setEmail("profile@pharmacy.com");

        when(authService.signup(signupRequest)).thenReturn(signupResponse);
        when(authService.login(loginRequest)).thenReturn(loginResponse);
        when(authService.getProfile("profile@pharmacy.com")).thenReturn(profile);

        assertThat(controller.signup(signupRequest).getBody()).isSameAs(signupResponse);
        assertThat(controller.login(loginRequest).getBody()).isSameAs(loginResponse);
        assertThat(controller.getProfile(userDetails).getBody()).isSameAs(profile);
    }

    @Test
    void adminUserController_ShouldListGetAndUpdateUsers() {
        UserRepository userRepository = mock(UserRepository.class);
        AdminUserController controller = new AdminUserController();
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);

        User user = createUser(1L, "Vikash", "vikash@pharmacy.com");
        User second = createUser(2L, "Aryan", "aryan@pharmacy.com");

        when(userRepository.findAll()).thenReturn(List.of(user, second));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(second));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(controller.listUsers().getBody()).hasSize(2);
        assertThat(controller.getUser(1L).getBody().getEmail()).isEqualTo("vikash@pharmacy.com");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setRole(Role.ADMIN);
        request.setActive(false);

        UserProfileDTO updated = controller.updateUser(2L, request).getBody();
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    void adminUserController_ShouldThrowWhenUserMissing() {
        UserRepository userRepository = mock(UserRepository.class);
        AdminUserController controller = new AdminUserController();
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getUser(404L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }

    @Test
    void jwtUtil_ShouldGenerateExtractAndValidateToken() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("user@pharmacy.com")
            .password("password")
            .authorities("ROLE_ADMIN")
            .build();

        String token = jwtUtil.generateToken(userDetails);

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("user@pharmacy.com");
        assertThat(jwtUtil.extractExpiration(token)).isAfter(new Date());
        assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();

        UserDetails otherUser = org.springframework.security.core.userdetails.User
            .withUsername("other@pharmacy.com")
            .password("password")
            .authorities("ROLE_ADMIN")
            .build();
        assertThat(jwtUtil.validateToken(token, otherUser)).isFalse();
    }

    @Test
    void jwtAuthFilter_ShouldHandleMissingInvalidAndValidTokens() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
        JwtAuthFilter filter = new JwtAuthFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(filter, "userDetailsService", userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer broken");
        when(jwtUtil.extractUsername("broken")).thenThrow(new IllegalArgumentException("bad"));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("user@pharmacy.com")
            .password("password")
            .authorities("ROLE_ADMIN")
            .build();
        when(jwtUtil.extractUsername("valid")).thenReturn("user@pharmacy.com");
        when(userDetailsService.loadUserByUsername("user@pharmacy.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken("valid", userDetails)).thenReturn(true);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
            .isEqualTo("user@pharmacy.com");

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("existing", null, List.of()));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        verify(jwtUtil, atLeastOnce()).extractUsername("valid");
    }

    @Test
    void globalExceptionHandler_ShouldBuildExpectedResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        Map<String, Object> badCredentialsBody = handler.handleBadCredentials(
            new BadCredentialsException("bad")).getBody();
        Map<String, Object> runtimeBody = handler.handleRuntime(
            new RuntimeException("runtime")).getBody();
        Map<String, Object> generalBody = handler.handleGeneral(
            new Exception("general")).getBody();

        assertThat(badCredentialsBody.get("status")).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(badCredentialsBody.get("message")).isEqualTo("Invalid email or password");
        assertThat(runtimeBody.get("message")).isEqualTo("runtime");
        assertThat(generalBody.get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void dtoAndEntityTypes_ShouldStoreValues() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@pharmacy.com");
        loginRequest.setPassword("secret");

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Signup");
        signupRequest.setEmail("signup@pharmacy.com");
        signupRequest.setMobile("9999999999");
        signupRequest.setPassword("secret");
        signupRequest.setAddress("Ara");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("token");
        authResponse.setTokenType("Bearer");
        authResponse.setUserId(9L);
        authResponse.setName("User");
        authResponse.setEmail("user@pharmacy.com");
        authResponse.setAddress("Ara");
        authResponse.setRole(Role.CUSTOMER);

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setId(5L);
        profileDTO.setName("Profile");
        profileDTO.setEmail("profile@pharmacy.com");
        profileDTO.setMobile("1111111111");
        profileDTO.setAddress("Address");
        profileDTO.setRole(Role.PHARMACIST);
        profileDTO.setActive(true);
        profileDTO.setCreatedAt(LocalDateTime.now());
        profileDTO.setUpdatedAt(LocalDateTime.now());

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setRole(Role.ADMIN);
        updateRequest.setActive(Boolean.TRUE);

        User user = createUser(7L, "Entity", "entity@pharmacy.com");
        user.setPassword("secret");
        user.setAddress("HQ");

        assertThat(loginRequest.getEmail()).isEqualTo("login@pharmacy.com");
        assertThat(loginRequest.getPassword()).isEqualTo("secret");
        assertThat(signupRequest.getAddress()).isEqualTo("Ara");
        assertThat(authResponse.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(profileDTO.getRole()).isEqualTo(Role.PHARMACIST);
        assertThat(updateRequest.getActive()).isTrue();
        assertThat(user.getAddress()).isEqualTo("HQ");
    }

    private User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setMobile("9999999999");
        user.setPassword("secret");
        user.setAddress("Address");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        return user;
    }

    @SuppressWarnings("unused")
    private String createSignedToken(String subject, long expirationMillis) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Jwts.builder()
            .subject(subject)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMillis))
            .signWith(Keys.hmacShaKeyFor(keyBytes))
            .compact();
    }
}
