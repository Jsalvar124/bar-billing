package com.jsalvar.barbilling.dto.request;

import com.jsalvar.barbilling.entity.Product;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemCreateRequestDto(
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        int quantity,

        String notes,
        @NotNull(message = "Product id is required")
        String productId,
        @NotNull(message = "Tab id is required")
        String tabId
) {
}
