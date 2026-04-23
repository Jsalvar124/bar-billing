package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.BarTableCreateRequestDto;
import com.jsalvar.barbilling.dto.request.BarTableUpdateRequestDto;
import com.jsalvar.barbilling.entity.BarTable;
import com.jsalvar.barbilling.entity.enums.TableStatus;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
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

    @Loggable
    @Override
    public List<BarTable> findAll() {
        return barTableRepository.findAll();
    }

    @Loggable
    @Override
    public BarTable findById(String id) {
        return barTableRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Table with id "+id +" not found"));
    }

    @Loggable
    @Override
    public BarTable findByNumber(String number) {
        return barTableRepository.findByNumber(number)
                .orElseThrow(()-> new ResourceNotFoundException("Table with number "+number+" not found"));
    }

    @Loggable
    @Override
    @Transactional
    public BarTable changeStatus(BarTable barTable, TableStatus tableStatus) {
        // find by id is checked by specific methods.
        barTable.setStatus(tableStatus);
        return barTableRepository.save(barTable);
    }

    @Loggable
    @Override
    @Transactional
    public BarTable reserveTable(String id){
        BarTable barTable = findById(id); // The service method already handles the error throw if not found.
        if(!barTable.getStatus().equals(TableStatus.AVAILABLE)){
            throw new UnprocessableEntityException("Error reserving table, table is not available");

        }
        return changeStatus(barTable, TableStatus.RESERVED);
    }

    @Loggable
    @Override
    @Transactional
    public BarTable cancelReservation(String id){
        BarTable barTable = findById(id); // check if exists
        if(!barTable.getStatus().equals(TableStatus.RESERVED)){
            throw new UnprocessableEntityException("Error canceling table reservation, table is not reserved");
        }
        return changeStatus(barTable, TableStatus.AVAILABLE);
    }

    @Loggable
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

    @Loggable
    @Override
    @Transactional
    public BarTable create(BarTableCreateRequestDto dto) {
        BarTable barTable = new BarTable();
        barTable.setCapacity(dto.capacity());
        barTable.setNumber(dto.number());
        return barTableRepository.save(barTable);
    }

    // Soft delete
    @Loggable
    @Override
    @Transactional
    public void delete(String id) {
        BarTable barTable = findById(id);
        if (!barTable.getStatus().equals(TableStatus.AVAILABLE)) {
            throw new UnprocessableEntityException("Error deleting table, it must be available before deleting");
        }
        barTable.setActive(false);
        barTableRepository.save(barTable);
    }
}
