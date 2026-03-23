package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.LoginRequestDto;
import com.jsalvar.barbilling.dto.request.RegisterRequestDto;
import com.jsalvar.barbilling.dto.response.LoginResponseDto;

public interface AuthService {
    String register(RegisterRequestDto requestDto);
    LoginResponseDto login(LoginRequestDto requestDto);
}
