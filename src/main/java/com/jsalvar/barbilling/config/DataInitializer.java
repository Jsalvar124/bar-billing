package com.jsalvar.barbilling.config;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.entity.enums.Role;
import com.jsalvar.barbilling.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminEmail;

    private final String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,@Value("${app.admin.email}") String adminEmail,@Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Loggable
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            UserImpl admin = new UserImpl();
            admin.setName("Admin");
            admin.setLastname("User");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
        }
    }
}
