package com.jsalvar.barbilling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BillCancelRequestDto(
        @NotBlank(message = "Bill id is required")
        String id,
        @NotBlank(message = "Cancellation reason is required")
        String cancellationReason
) {

}
