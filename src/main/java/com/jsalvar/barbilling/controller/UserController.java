package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.ChangePasswordRequestDto;
import com.jsalvar.barbilling.dto.request.UserCreateRequestDto;
import com.jsalvar.barbilling.dto.request.UserUpdateRequestDto;
import com.jsalvar.barbilling.dto.response.UserResponseDto;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Loggable
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> findAll() {
        List<UserResponseDto> users = userService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Loggable
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> findById(@PathVariable String id) {
        UserImpl user = userService.findById(id);
        return ResponseEntity.ok(toDto(user));
    }

    @Loggable
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        UserImpl user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(toDto(user));
    }

    @Loggable
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> create(@RequestBody UserCreateRequestDto dto) {
        UserImpl user = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(user));
    }

    @Loggable
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable String id,
            @RequestBody UserUpdateRequestDto dto) {
        UserImpl user = userService.update(id, dto);
        return ResponseEntity.ok(toDto(user));
    }

    @Loggable
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Loggable
    @PatchMapping("/{id}/password")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> changePassword(
            @PathVariable String id,
            @RequestBody ChangePasswordRequestDto dto) {
        userService.changePassword(id, dto);
        return ResponseEntity.noContent().build();
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
}
