package com.jsalvar.barbilling.dto.request;

import com.jsalvar.barbilling.entity.enums.TableStatus;

public record BarTableCreateRequestDto(
        String number,
        Integer capacity
) {
}
