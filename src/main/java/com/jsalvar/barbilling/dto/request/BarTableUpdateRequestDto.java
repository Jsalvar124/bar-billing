package com.jsalvar.barbilling.dto.request;

import com.jsalvar.barbilling.entity.enums.TableStatus;

public record BarTableUpdateRequestDto(
        String id,
        String number,
        Integer capacity
) {
}
