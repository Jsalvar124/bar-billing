package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.LoginRequestDto;
import com.jsalvar.barbilling.dto.request.RegisterRequestDto;
import com.jsalvar.barbilling.dto.response.LoginResponseDto;
import com.jsalvar.barbilling.entity.enums.Role;
import com.jsalvar.barbilling.service.AuthService;
import com.jsalvar.barbilling.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto requestDto){
        if (requestDto.role() == Role.ADMIN) {
            return ResponseEntity.badRequest().body("Cannot self-register as ADMIN");
        }

        String id = authService.register(requestDto);
        URI location = URI.create("/api/v1/users/" + id);
        return ResponseEntity.created(location).body("Successful registration, id: " + id);
    }

    @Loggable
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto){
        LoginResponseDto response = authService.login(requestDto);
         return ResponseEntity.ok().body(response);
    }



}
