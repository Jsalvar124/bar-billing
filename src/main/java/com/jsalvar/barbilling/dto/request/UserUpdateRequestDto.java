package com.jsalvar.barbilling.dto.request;

import com.jsalvar.barbilling.entity.enums.Role;

public record UserUpdateRequestDto(
        String name,
        String lastname,
        String email,
        Role role
) {
}
