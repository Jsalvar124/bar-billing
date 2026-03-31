package com.jsalvar.barbilling.user;

import com.jsalvar.barbilling.dto.request.ChangePasswordRequestDto;
import com.jsalvar.barbilling.dto.request.UserCreateRequestDto;
import com.jsalvar.barbilling.dto.request.UserUpdateRequestDto;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.entity.enums.Role;
import com.jsalvar.barbilling.repository.UserRepository;
import com.jsalvar.barbilling.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserImpl testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserImpl();
        testUser.setId("user-id-123");
        testUser.setName("John");
        testUser.setLastname("Doe");
        testUser.setEmail("john@bar.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.WAITER);
        testUser.setActive(true);
    }

    @Test
    void create_Success() {
        UserCreateRequestDto dto = new UserCreateRequestDto(
                "John", "Doe", "john@bar.com", "password123", Role.WAITER);

        when(userRepository.existsByEmail("john@bar.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserImpl.class))).thenAnswer(invocation -> {
            UserImpl user = invocation.getArgument(0);
            user.setId("new-user-id");
            return user;
        });

        UserImpl result = userService.create(dto);

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals("john@bar.com", result.getEmail());
        assertEquals(Role.WAITER, result.getRole());
        assertTrue(result.isActive());
        verify(userRepository).save(any(UserImpl.class));
    }

    @Test
    void create_EmailAlreadyExists() {
        UserCreateRequestDto dto = new UserCreateRequestDto(
                "John", "Doe", "john@bar.com", "password123", Role.WAITER);

        when(userRepository.existsByEmail("john@bar.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.create(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_EncodesPassword() {
        UserCreateRequestDto dto = new UserCreateRequestDto(
                "John", "Doe", "john@bar.com", "plainPassword", Role.WAITER);

        when(userRepository.existsByEmail("john@bar.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        UserImpl result = userService.create(dto);

        assertEquals("hashedPassword", result.getPassword());
    }

    @Test
    void update_Success() {
        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                "Jane", "Smith", "jane@bar.com", Role.ADMIN);

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("jane@bar.com")).thenReturn(false);
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        UserImpl result = userService.update("user-id-123", dto);

        assertEquals("Jane", result.getName());
        assertEquals("Smith", result.getLastname());
        assertEquals("jane@bar.com", result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
    }

    @Test
    void update_PartialUpdate() {
        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                "Jane", null, null, null);

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        UserImpl result = userService.update("user-id-123", dto);

        assertEquals("Jane", result.getName());
        assertEquals("Doe", result.getLastname());
        assertEquals("john@bar.com", result.getEmail());
    }

    @Test
    void update_EmailChange_Success() {
        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                null, null, "newemail@bar.com", null);

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@bar.com")).thenReturn(false);
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        UserImpl result = userService.update("user-id-123", dto);

        assertEquals("newemail@bar.com", result.getEmail());
    }

    @Test
    void update_EmailAlreadyExists() {
        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                null, null, "existing@bar.com", null);

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@bar.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, 
                () -> userService.update("user-id-123", dto));
    }

    @Test
    void update_RoleChanged() {
        UserUpdateRequestDto dto = new UserUpdateRequestDto(
                null, null, null, Role.ADMIN);

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        UserImpl result = userService.update("user-id-123", dto);

        assertEquals(Role.ADMIN, result.getRole());
    }

    @Test
    void delete_SoftDelete() {
        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.delete("user-id-123");

        assertFalse(testUser.isActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_CorrectOld() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto("oldPassword", "newPassword");

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(UserImpl.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.changePassword("user-id-123", dto);

        assertEquals("newEncodedPassword", testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_WrongOld() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto("wrongPassword", "newPassword");

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, 
                () -> userService.changePassword("user-id-123", dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findById_Success() {
        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(testUser));

        UserImpl result = userService.findById("user-id-123");

        assertNotNull(result);
        assertEquals("user-id-123", result.getId());
    }

    @Test
    void findById_NotFound() {
        when(userRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, 
                () -> userService.findById("unknown-id"));
    }

    @Test
    void findByEmail_Success() {
        when(userRepository.findByEmail("john@bar.com")).thenReturn(Optional.of(testUser));

        UserImpl result = userService.findByEmail("john@bar.com");

        assertNotNull(result);
        assertEquals("john@bar.com", result.getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        when(userRepository.findByEmail("unknown@bar.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, 
                () -> userService.findByEmail("unknown@bar.com"));
    }

    @Test
    void existsByEmail_True() {
        when(userRepository.existsByEmail("john@bar.com")).thenReturn(true);

        boolean result = userService.existsByEmail("john@bar.com");

        assertTrue(result);
    }

    @Test
    void existsByEmail_False() {
        when(userRepository.existsByEmail("unknown@bar.com")).thenReturn(false);

        boolean result = userService.existsByEmail("unknown@bar.com");

        assertFalse(result);
    }

    @Test
    void findAll_ReturnsList() {
        when(userRepository.findAll()).thenReturn(java.util.List.of(testUser));

        var result = userService.findAll();

        assertEquals(1, result.size());
    }
}
