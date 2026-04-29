package com.jsalvar.barbilling.dto.response;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        String id,
        int quantity,
        String notes,
        String productId,
        String productName,
        BigDecimal unitPrice,
        String tabId,
        BigDecimal subtotal
) {
}
