package com.jsalvar.barbilling.dto.request;

import com.jsalvar.barbilling.entity.enums.TabStatus;

import java.time.LocalDate;

public record TabCreateRequestDto(
        String tableId,
        String waiterId
) {
}
