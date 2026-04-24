package com.jsalvar.barbilling.dto.request;

import java.math.BigDecimal;

public record TaxRateCreateRequestDto(
        String name,
        BigDecimal rate
) {
}