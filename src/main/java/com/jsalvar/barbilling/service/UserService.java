package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.RegisterRequestDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService{
    String register(RegisterRequestDto requestDto);
}
