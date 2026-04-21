package com.jsalvar.barbilling.dto.response;

import com.jsalvar.barbilling.entity.enums.TableStatus;
import jakarta.persistence.*;

public record BarTableResponseDto(
    String id,
    String number,
    Integer capacity,
    TableStatus status
) {
}
