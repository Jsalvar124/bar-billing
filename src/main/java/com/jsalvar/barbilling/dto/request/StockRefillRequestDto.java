package com.jsalvar.barbilling.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockRefillRequestDto(
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        int quantity
) {
}
