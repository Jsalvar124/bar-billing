package com.jsalvar.barbilling.dto;

import com.jsalvar.barbilling.entity.Role;

public record RegisterRequestDto(
        String name,
        String lastname,
        String email,
        String password,
        Role role // "ADMIN", "CASHIER", "WAITER"
) {
}
