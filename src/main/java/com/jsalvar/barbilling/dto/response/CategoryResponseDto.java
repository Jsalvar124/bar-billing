package com.jsalvar.barbilling.dto.response;

import com.jsalvar.barbilling.entity.enums.KitchenType;

import java.util.Set;

public record CategoryResponseDto(
        String id,
        String name,
        KitchenType kitchenType,
        Set<TaxRateInfo> taxRates
) {
    public record TaxRateInfo(
            String id,
            String name
    ) {}
}