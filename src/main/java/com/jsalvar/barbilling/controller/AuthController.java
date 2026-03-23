package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.dto.RegisterRequestDto;
import com.jsalvar.barbilling.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto requestDto){
        String id = userService.register(requestDto);
        URI location = URI.create("/api/v1/users/" + id);
        return ResponseEntity.created(location).body(id);

    }


}
