package com.jsalvar.barbilling.dto.response;

import com.jsalvar.barbilling.entity.enums.KitchenType;

import java.math.BigDecimal;

public record ProductResponseDto(
        String id,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        CategroyInfo categoryInfo
) {
    public record CategroyInfo(
            String id,
            String name,
            KitchenType kitchenType
    ) {}
}
