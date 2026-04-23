package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.BarTableCreateRequestDto;
import com.jsalvar.barbilling.dto.request.BarTableUpdateRequestDto;
import com.jsalvar.barbilling.entity.BarTable;
import com.jsalvar.barbilling.entity.enums.TableStatus;

import java.util.List;

public interface BarTableService {
    List<BarTable> findAll();
    BarTable findById(String id);
    BarTable findByNumber(String number);
    BarTable reserveTable(String id);
    BarTable cancelReservation(String id);
    BarTable changeStatus(BarTable barTable, TableStatus tableStatus);
    BarTable update(String id, BarTableUpdateRequestDto dto);
    BarTable create(BarTableCreateRequestDto dto);
    void delete(String id);
}
