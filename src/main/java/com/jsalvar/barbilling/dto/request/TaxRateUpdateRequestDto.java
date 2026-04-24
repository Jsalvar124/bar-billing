package com.jsalvar.barbilling.dto.request;

import java.math.BigDecimal;

public record TaxRateUpdateRequestDto(
        String name,
        BigDecimal rate
) {
}