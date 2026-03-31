package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.response.UserResponseDto;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Loggable
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        List<UserResponseDto> users = userService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Loggable
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findById(@PathVariable String id) {
        UserImpl user = userService.findById(id);


        return ResponseEntity.ok(toDto(user));
    }

    private UserResponseDto toDto(UserImpl user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                user.getRole()
        );
    }
}
