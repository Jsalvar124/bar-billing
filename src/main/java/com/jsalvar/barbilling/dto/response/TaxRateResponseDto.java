package com.jsalvar.barbilling.dto.response;

import java.math.BigDecimal;

public record TaxRateResponseDto(
        String id,
        String name,
        BigDecimal rate
) {
}