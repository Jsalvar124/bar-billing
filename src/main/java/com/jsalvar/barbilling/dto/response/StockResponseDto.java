package com.jsalvar.barbilling.dto.response;

import com.jsalvar.barbilling.entity.Product;
import jakarta.persistence.*;

public record StockResponseDto(
        String productId,
        String productName,
        int quantity,
        int lowStockThreshold
) {
}
