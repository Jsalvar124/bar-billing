package com.jsalvar.barbilling.auth;

import com.jsalvar.barbilling.controller.AuthController;
import com.jsalvar.barbilling.dto.request.LoginRequestDto;
import com.jsalvar.barbilling.dto.request.RegisterRequestDto;
import com.jsalvar.barbilling.dto.response.LoginResponseDto;
import com.jsalvar.barbilling.entity.enums.Role;
import com.jsalvar.barbilling.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_WithValidRole_Returns201() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "John", "Doe", "john@bar.com", "password123", Role.WAITER);
        when(authService.register(any())).thenReturn("user-id-123");

        ResponseEntity<?> response = authController.register(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("user-id-123"));
        verify(authService).register(dto);
    }

    @Test
    void register_WithAdminRole_Returns400() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "John", "Doe", "john@bar.com", "password123", Role.ADMIN);

        ResponseEntity<?> response = authController.register(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cannot self-register as ADMIN", response.getBody());
        verify(authService, never()).register(any());
    }

    @Test
    void register_PassesRequestToService() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "Jane", "Smith", "jane@bar.com", "password123", Role.CASHIER);
        when(authService.register(any())).thenReturn("new-user-id");

        authController.register(dto);

        verify(authService).register(dto);
    }

    @Test
    void login_WithValidCredentials_Returns200() {
        LoginRequestDto dto = new LoginRequestDto("john@bar.com", "password123");
        when(authService.login(any())).thenReturn(new LoginResponseDto("jwt-token-abc123"));

        ResponseEntity<LoginResponseDto> response = authController.login(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token-abc123", response.getBody().token());
    }

    @Test
    void login_WithInvalidCredentials_Returns401() {
        LoginRequestDto dto = new LoginRequestDto("john@bar.com", "wrongPassword");
        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authController.login(dto));
    }

    @Test
    void login_PassesRequestToService() {
        LoginRequestDto dto = new LoginRequestDto("john@bar.com", "password123");
        when(authService.login(any())).thenReturn(new LoginResponseDto("jwt-token"));

        authController.login(dto);

        verify(authService).login(dto);
    }
}
