package com.jsalvar.barbilling.dto.request;

import java.math.BigDecimal;

public record ProductUpdateRecordDto(
        String id,
        String name,
        String description,
        BigDecimal price,
        String categoryId
) {
}
