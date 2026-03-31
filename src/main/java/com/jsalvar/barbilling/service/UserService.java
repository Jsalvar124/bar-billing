package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.LoginRequestDto;
import com.jsalvar.barbilling.dto.request.RegisterRequestDto;
import com.jsalvar.barbilling.dto.response.LoginResponseDto;
import com.jsalvar.barbilling.entity.UserImpl;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService{
    List<UserImpl> findAll();
    UserImpl findById(String id);
}
