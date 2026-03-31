package com.jsalvar.barbilling.dto.request;

public record LoginRequestDto(
        String email,
        String password
) {
    @Override
    public String toString() {
        return "LoginRequestDto{" +
                "email='" + email + '\'' +
                ", password=***'" + '\'' +
                '}';
    }
}
