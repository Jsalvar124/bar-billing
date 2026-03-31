package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.ChangePasswordRequestDto;
import com.jsalvar.barbilling.dto.request.UserCreateRequestDto;
import com.jsalvar.barbilling.dto.request.UserUpdateRequestDto;
import com.jsalvar.barbilling.entity.UserImpl;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    List<UserImpl> findAll();
    UserImpl findById(String id);
    UserImpl findByEmail(String email);
    boolean existsByEmail(String email);
    UserImpl create(UserCreateRequestDto dto);
    UserImpl update(String id, UserUpdateRequestDto dto);
    void delete(String id);
    void changePassword(String id, ChangePasswordRequestDto dto);
}
