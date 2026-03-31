package com.jsalvar.barbilling.dto.request;

import com.jsalvar.barbilling.entity.enums.Role;

public record RegisterRequestDto(
        String name,
        String lastname,
        String email,
        String password,
        Role role // "ADMIN", "CASHIER", "WAITER"
) {
}
