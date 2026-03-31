package com.jsalvar.barbilling.user;

import com.jsalvar.barbilling.controller.UserController;
import com.jsalvar.barbilling.dto.request.ChangePasswordRequestDto;
import com.jsalvar.barbilling.dto.request.UserCreateRequestDto;
import com.jsalvar.barbilling.dto.request.UserUpdateRequestDto;
import com.jsalvar.barbilling.dto.response.UserResponseDto;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.entity.enums.Role;
import com.jsalvar.barbilling.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserImpl testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserImpl();
        testUser.setId("user-id-123");
        testUser.setName("John");
        testUser.setLastname("Doe");
        testUser.setEmail("john@bar.com");
        testUser.setRole(Role.ADMIN);
        testUser.setActive(true);
    }

    private UserResponseDto toDto(UserImpl user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }

    @Test
    void findAll_AsAdmin_Returns200() {
        when(userService.findAll()).thenReturn(List.of(testUser));

        ResponseEntity<List<UserResponseDto>> response = userController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("john@bar.com", response.getBody().get(0).email());
    }

    @Test
    void findById_Success_Returns200() {
        when(userService.findById("user-id-123")).thenReturn(testUser);

        ResponseEntity<UserResponseDto> response = userController.findById("user-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("john@bar.com", response.getBody().email());
    }

    @Test
    void findById_NotFound_Returns404() {
        when(userService.findById("unknown-id")).thenThrow(
                new org.springframework.security.core.userdetails.UsernameNotFoundException("Not found"));

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userController.findById("unknown-id"));
    }

    @Test
    void getMe_Authenticated_Returns200() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john@bar.com");
        when(userService.findByEmail("john@bar.com")).thenReturn(testUser);

        ResponseEntity<UserResponseDto> response = userController.getCurrentUser(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("john@bar.com", response.getBody().email());
    }

    @Test
    void create_Success_Returns201() {
        UserCreateRequestDto dto = new UserCreateRequestDto("Jane", "Doe", "jane@bar.com", "password", Role.WAITER);
        when(userService.create(any())).thenReturn(testUser);

        ResponseEntity<UserResponseDto> response = userController.create(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void update_Success_Returns200() {
        UserUpdateRequestDto dto = new UserUpdateRequestDto("Jane", "Smith", "jane@bar.com", Role.ADMIN);
        when(userService.update(eq("user-id-123"), any())).thenReturn(testUser);

        ResponseEntity<UserResponseDto> response = userController.update("user-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void delete_Success_Returns204() {
        doNothing().when(userService).delete("user-id-123");

        ResponseEntity<Void> response = userController.delete("user-id-123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).delete("user-id-123");
    }

    @Test
    void changePassword_CorrectOld_Returns204() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto("oldPassword", "newPassword");
        doNothing().when(userService).changePassword(eq("user-id-123"), any());

        ResponseEntity<Void> response = userController.changePassword("user-id-123", dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void changePassword_WrongOld_Returns401() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto("wrongPassword", "newPassword");
        doThrow(new BadCredentialsException("Wrong password"))
                .when(userService).changePassword(eq("user-id-123"), any());

        assertThrows(BadCredentialsException.class,
                () -> userController.changePassword("user-id-123", dto));
    }
}
