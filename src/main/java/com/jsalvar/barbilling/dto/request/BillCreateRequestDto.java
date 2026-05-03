package com.jsalvar.barbilling.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BillCreateRequestDto(
        @NotBlank(message = "Tab id is required")
        String tabId,
        @NotBlank(message = "Cashier id is required")
        String cashierId,
        @NotNull(message = "Tip value is required")
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "1.0") // max 100% tip
        BigDecimal tip
) {
}
