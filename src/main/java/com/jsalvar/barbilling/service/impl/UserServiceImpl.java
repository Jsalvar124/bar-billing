package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.RegisterRequestDto;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.repository.UserRepository;
import com.jsalvar.barbilling.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // For spring security
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    @Override
    @Loggable
    @Transactional
    public String register(RegisterRequestDto requestDto) {
        // Verify email does not exist
        if(userRepository.existsByEmail(requestDto.email())){
            throw new IllegalArgumentException("Unprocessable request - email already exists");
        }

        UserImpl user = new UserImpl();
        user.setName(requestDto.name());
        user.setLastname(requestDto.lastname());
        user.setEmail(requestDto.email());
        user.setRole(requestDto.role());

        // Save hashed password
        String encodedPassword = passwordEncoder.encode(requestDto.password());
        user.setPassword(encodedPassword);

        UserImpl result = userRepository.save(user);
        return result.getId();
    }

}
