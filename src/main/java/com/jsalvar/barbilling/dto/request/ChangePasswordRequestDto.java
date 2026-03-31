package com.jsalvar.barbilling.dto.request;

public record ChangePasswordRequestDto(
        String oldPassword,
        String newPassword
) {
}
