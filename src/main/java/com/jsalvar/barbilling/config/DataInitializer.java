package com.jsalvar.barbilling.config;

import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.entity.enums.Role;
import com.jsalvar.barbilling.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            UserImpl admin = new UserImpl();
            admin.setName("Admin");
            admin.setLastname("User");
            admin.setEmail("juana@bar.com");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
        }
    }
}
