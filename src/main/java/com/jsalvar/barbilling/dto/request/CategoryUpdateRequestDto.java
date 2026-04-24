package com.jsalvar.barbilling.dto.request;

import com.jsalvar.barbilling.entity.enums.KitchenType;

public record CategoryUpdateRequestDto(
        String name,
        KitchenType kitchenType
) {
}