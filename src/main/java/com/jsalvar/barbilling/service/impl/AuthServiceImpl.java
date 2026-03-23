package com.jsalvar.barbilling.service.impl;


import com.jsalvar.barbilling.dto.request.LoginRequestDto;
import com.jsalvar.barbilling.dto.request.RegisterRequestDto;
import com.jsalvar.barbilling.dto.response.LoginResponseDto;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.UserRepository;
import com.jsalvar.barbilling.service.AuthService;
import com.jsalvar.barbilling.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String register(RegisterRequestDto requestDto) {
        // Verify email does not exist
        if(userRepository.existsByEmail(requestDto.email())){
            throw new UnprocessableEntityException("Unprocessable request - email already exists");
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

    @Override
    public LoginResponseDto login(LoginRequestDto requestDto) {
        // Use the authentication manager bean, create a UsernamePasswordAuthtoken
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.email(),
                        requestDto.password()
                )
        );

        UserDetails user = (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(user);

        return new LoginResponseDto(token);    }
}
