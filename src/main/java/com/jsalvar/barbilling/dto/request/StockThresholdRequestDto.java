package com.jsalvar.barbilling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockThresholdRequestDto(
        @NotNull(message = "Threshold is required")
        @Positive(message = "Threshold must be positive")
        int threshold
) {
}
