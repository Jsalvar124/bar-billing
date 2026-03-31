package com.jsalvar.barbilling.dto.request;

import com.jsalvar.barbilling.entity.enums.Role;

public record UserCreateRequestDto(
        String name,
        String lastname,
        String email,
        String password,
        Role role
) {
}
