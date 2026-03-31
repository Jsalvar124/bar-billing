package com.jsalvar.barbilling.auth;

import com.jsalvar.barbilling.dto.request.LoginRequestDto;
import com.jsalvar.barbilling.dto.request.RegisterRequestDto;
import com.jsalvar.barbilling.dto.response.LoginResponseDto;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.entity.enums.Role;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.UserRepository;
import com.jsalvar.barbilling.service.JwtService;
import com.jsalvar.barbilling.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserImpl savedUser;

    @BeforeEach
    void setUp() {
        savedUser = new UserImpl();
        savedUser.setId("user-id-123");
        savedUser.setName("John");
        savedUser.setLastname("Doe");
        savedUser.setEmail("john@bar.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(Role.WAITER);
        savedUser.setActive(true);
    }

    @Test
    void register_Success() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "John", "Doe", "john@bar.com", "password123", Role.WAITER);

        when(userRepository.existsByEmail("john@bar.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserImpl.class))).thenAnswer(invocation -> {
            UserImpl user = invocation.getArgument(0);
            user.setId("user-id-123");
            return user;
        });

        String result = authService.register(dto);

        assertNotNull(result);
        assertEquals("user-id-123", result);
        verify(userRepository).save(any(UserImpl.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "John", "Doe", "john@bar.com", "password123", Role.WAITER);

        when(userRepository.existsByEmail("john@bar.com")).thenReturn(true);

        assertThrows(UnprocessableEntityException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_EncodesPassword() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "John", "Doe", "john@bar.com", "plainPassword", Role.WAITER);

        when(userRepository.existsByEmail("john@bar.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(dto);

        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    void register_SetsDefaultActive() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "John", "Doe", "john@bar.com", "password123", Role.WAITER);

        when(userRepository.existsByEmail("john@bar.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(dto);

        verify(userRepository).save(argThat(user -> user.isActive() == true));
    }

    @Test
    void register_SetsAllFields() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "John", "Doe", "john@bar.com", "password123", Role.CASHIER);

        when(userRepository.existsByEmail("john@bar.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(dto);

        verify(userRepository).save(argThat(user ->
                user.getName().equals("John") &&
                user.getLastname().equals("Doe") &&
                user.getEmail().equals("john@bar.com") &&
                user.getRole() == Role.CASHIER
        ));
    }

    @Test
    void login_Success() {
        LoginRequestDto dto = new LoginRequestDto("john@bar.com", "password123");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        LoginResponseDto result = authService.login(dto);

        assertNotNull(result);
        assertEquals("jwt-token", result.token());
    }

    @Test
    void login_InvalidCredentials() {
        LoginRequestDto dto = new LoginRequestDto("john@bar.com", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }

    @Test
    void login_GeneratesToken() {
        LoginRequestDto dto = new LoginRequestDto("john@bar.com", "password123");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        authService.login(dto);

        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void login_CallsAuthenticationManager() {
        LoginRequestDto dto = new LoginRequestDto("john@bar.com", "password123");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        authService.login(dto);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("john@bar.com", "password123")
        );
    }

    @Test
    void login_CallsJwtService() {
        LoginRequestDto dto = new LoginRequestDto("john@bar.com", "password123");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        authService.login(dto);

        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void login_ReturnsTokenInResponse() {
        LoginRequestDto dto = new LoginRequestDto("john@bar.com", "password123");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token-abc123");

        LoginResponseDto result = authService.login(dto);

        assertEquals("jwt-token-abc123", result.token());
    }
}
