package com.jsalvar.barbilling.dto.request;

public record ChangePasswordRequestDto(
        String oldPassword,
        String newPassword
) {
    @Override
    public String toString() {
        return "ChangePasswordRequestDto[oldPassword=***, newPassword=***]";
    }
}
