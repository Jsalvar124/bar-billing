package com.jsalvar.barbilling.dto.response;

import com.jsalvar.barbilling.entity.enums.Role;

public record UserResponseDto(
        String id,
        String name,
        String lastname,
        String email,
        Role role,
        boolean active
) {
}
