package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.BarTableCreateRequestDto;
import com.jsalvar.barbilling.dto.request.BarTableUpdateRequestDto;
import com.jsalvar.barbilling.entity.BarTable;
import com.jsalvar.barbilling.entity.enums.TableStatus;
import com.jsalvar.barbilling.repository.BarTableRepository;
import com.jsalvar.barbilling.service.BarTableService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BarTableImpl implements BarTableService {
    private final BarTableRepository barTableRepository;

    public BarTableImpl(BarTableRepository barTableRepository) {
        this.barTableRepository = barTableRepository;
    }

    @Override
    public List<BarTable> findAll() {
        return barTableRepository.findAll();
    }

    @Override
    public BarTable findById(String id) {
        return barTableRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Table with given id not found"));
    }

    @Override
    public BarTable findByNumber(String number) {
        return barTableRepository.findByNumber(number)
                .orElseThrow(()-> new EntityNotFoundException("Table with given number not found"));
    }

    @Override
    @Transactional
    @Loggable
    public void changeStatus(String id, TableStatus tableStatus) {
        // Retrieve table
        BarTable barTable = findById(id); // The service method already handles the error throw if not found.
        barTable.setStatus(tableStatus);
        barTableRepository.save(barTable);
    }

    @Override
    @Transactional
    public BarTable update(String id, BarTableUpdateRequestDto dto) {
        BarTable barTable = findById(id);
        if (dto.number() != null && !dto.number().isBlank()) {
            barTable.setNumber(dto.number());
        }

        if (dto.capacity() != null && dto.capacity() > 0) {
            barTable.setCapacity(dto.capacity());
        }
        return barTableRepository.save(barTable);
    }

    @Override
    @Transactional
    public BarTable create(BarTableCreateRequestDto dto) {
        BarTable barTable = new BarTable();
        barTable.setCapacity(dto.capacity());
        barTable.setNumber(dto.number());
        return barTable;
    }

    @Override
    @Transactional
    public void delete(String id) {
        BarTable barTable = findById(id);
        barTableRepository.delete(barTable);
    }
}
