package com.jsalvar.barbilling.dto.request;

public record LoginRequestDto(
        String email,
        String password
) {
}
