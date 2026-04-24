package com.jsalvar.barbilling.dto.request;

import com.jsalvar.barbilling.entity.enums.TabStatus;

import java.time.LocalDate;

public record TabSearchRequestDto(
        String tableId,
        String waiterId,
        String status,
        LocalDate from,
        LocalDate to
) {
}
