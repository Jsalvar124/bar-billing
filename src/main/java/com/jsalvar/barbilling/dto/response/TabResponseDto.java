package com.jsalvar.barbilling.dto.response;

import com.jsalvar.barbilling.entity.enums.TabStatus;

import java.time.LocalDateTime;

public record TabResponseDto(
        String id,
        String tableId,
        String tableNumber,   // useful so the frontend doesn't need a second call
        String waiterId,
        String waiterName,    // same reason
        TabStatus status,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
}
