package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.dto.request.TabCreateRequestDto;
import com.jsalvar.barbilling.dto.request.TabSearchRequestDto;
import com.jsalvar.barbilling.entity.BarTable;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.entity.enums.Role;
import com.jsalvar.barbilling.entity.enums.TabStatus;
import com.jsalvar.barbilling.entity.enums.TableStatus;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.TabRepository;
import com.jsalvar.barbilling.service.BarTableService;
import com.jsalvar.barbilling.service.TabService;
import com.jsalvar.barbilling.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TabServiceImpl implements TabService {
    private final TabRepository tabRepository;
    private final BarTableService barTableService;
    private final UserService userService;

    public TabServiceImpl(TabRepository tabRepository, BarTableService barTableService, UserService userService) {
        this.tabRepository = tabRepository;
        this.barTableService = barTableService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Tab create(TabCreateRequestDto dto) {
        // find waiter
        UserImpl waiter = userService.findById(dto.waiterId());
        // check waiter role
        if(!waiter.getRole().equals(Role.WAITER) && !waiter.getRole().equals(Role.ADMIN)){
            throw new UnprocessableEntityException("Selected user is not a waiter");
        }
        BarTable table = barTableService.findById(dto.tableId());
        // Check if table is not occupied
        if (!table.getStatus().equals(TableStatus.AVAILABLE)) {
            throw new UnprocessableEntityException("Table is not available");
        }


        Tab tab = Tab.builder()
                .status(TabStatus.OPEN)
                .waiter(waiter)
                .table(table)
                .build();

        // Change table status
        barTableService.changeStatus(table, TableStatus.OCCUPIED);

        return tabRepository.save(tab);
    }

    @Override
    @Transactional
    public Tab closeTab(String id) {
        Tab tab = tabRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("Tab not found"));
        if(!tab.getStatus().equals(TabStatus.OPEN)){
            throw new UnprocessableEntityException("Selected tab is cancelled or already closed");
        }
        tab.setStatus(TabStatus.CLOSED);
        tab.setClosedAt(LocalDateTime.now()); // add close timestamp
        return tabRepository.save(tab);
    }

    @Override
    @Transactional
    public Tab cancelTab(String id) {
        Tab tab = tabRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("Tab not found"));
        if(!tab.getStatus().equals(TabStatus.OPEN)){
            throw new UnprocessableEntityException("Only Open tabs can be cancelled");
        }
        BarTable table = tab.getTable();
        barTableService.changeStatus(table, TableStatus.AVAILABLE); // Change table status
        tab.setStatus(TabStatus.CANCELED);
        tab.setClosedAt(LocalDateTime.now()); // add close timestamp

        return tabRepository.save(tab);
    }

    @Override
    public Tab findById(String id) {
        return tabRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Tab with id "+ id + " not found"));
    }

    @Override
    public List<Tab> findByStatus(TabStatus status) {
        return tabRepository.findByStatus(status);
    }

    @Override
    public List<Tab> searchTab(TabSearchRequestDto dto) {
        return tabRepository.searchTab(
                dto.tableId(),
                dto.waiterId(),
                dto.status(),
                dto.from(),
                dto.to()
        );
    }

}
