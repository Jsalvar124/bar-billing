package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")

public class UserController {
    @Loggable
    @GetMapping("/test")
    public String test(Authentication authentication) {
        System.out.println(authentication.getAuthorities());
        return "ok";
    }
}
