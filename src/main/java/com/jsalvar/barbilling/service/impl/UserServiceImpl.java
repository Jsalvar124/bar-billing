package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.dto.request.ChangePasswordRequestDto;
import com.jsalvar.barbilling.dto.request.UserCreateRequestDto;
import com.jsalvar.barbilling.dto.request.UserUpdateRequestDto;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.repository.UserRepository;
import com.jsalvar.barbilling.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserImpl> findAll() {
        return userRepository.findAll();
    }

    @Override
    public UserImpl findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public UserImpl findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserImpl create(UserCreateRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        UserImpl user = new UserImpl();
        user.setName(dto.name());
        user.setLastname(dto.lastname());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(dto.role());
        user.setActive(true);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserImpl update(String id, UserUpdateRequestDto dto) {
        UserImpl user = findById(id);

        if (dto.name() != null) {
            user.setName(dto.name());
        }
        if (dto.lastname() != null) {
            user.setLastname(dto.lastname());
        }
        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.email())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(dto.email());
        }
        if (dto.role() != null) {
            user.setRole(dto.role());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(String id) {
        UserImpl user = findById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String id, ChangePasswordRequestDto dto) {
        UserImpl user = findById(id);

        if (!passwordEncoder.matches(dto.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }
}
