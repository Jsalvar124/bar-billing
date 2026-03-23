package com.jsalvar.barbilling.dto.response;

public record ErrorResponseDto(
        String error,
        String message,
        String timestamp,
        int status
) {
}
