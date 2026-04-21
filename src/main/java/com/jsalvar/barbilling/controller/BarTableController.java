package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.BarTableCreateRequestDto;
import com.jsalvar.barbilling.dto.request.BarTableUpdateRequestDto;
import com.jsalvar.barbilling.dto.response.BarTableResponseDto;
import com.jsalvar.barbilling.entity.BarTable;
import com.jsalvar.barbilling.service.BarTableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tables")
public class BarTableController {
    private final BarTableService barTableService;

    public BarTableController(BarTableService barTableService) {
        this.barTableService = barTableService;
    }

    @Loggable
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BarTableResponseDto>> findAll() {
        List<BarTableResponseDto> users = barTableService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Loggable
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BarTableResponseDto> findById(@PathVariable String id) {
        BarTable table = barTableService.findById(id);
        return ResponseEntity.ok(toDto(table));
    }

    @Loggable
    @GetMapping("/number/{number}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BarTableResponseDto> findByNumber(@PathVariable String number) {
        BarTable table = barTableService.findByNumber(number);
        return ResponseEntity.ok(toDto(table));
    }

    @Loggable
    @PatchMapping("/{id}/reserve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAITER')")
    public ResponseEntity<BarTableResponseDto> reserveTable(@PathVariable String id) {
        BarTable table = barTableService.reserveTable(id);
        return ResponseEntity.ok(toDto(table));
    }

    @Loggable
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAITER')")
    public ResponseEntity<BarTableResponseDto> cancelTableReservation(@PathVariable String id) {
        BarTable table = barTableService.cancelReservation(id);
        return ResponseEntity.ok(toDto(table));
    }

    @Loggable
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BarTableResponseDto> create(@RequestBody BarTableCreateRequestDto dto){
        BarTable barTable = barTableService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(barTable));
    }

    @Loggable
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BarTableResponseDto> update(
            @PathVariable String id,
            @RequestBody BarTableUpdateRequestDto dto){
        BarTable barTable = barTableService.update(id, dto);
        return ResponseEntity.ok().body(toDto(barTable));
    }

    @Loggable
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id){
        barTableService.delete(id);
        return ResponseEntity.noContent().build();
    }



    private BarTableResponseDto toDto(BarTable barTable){
        return new BarTableResponseDto(
                barTable.getId(), // UUID from database
                barTable.getNumber(),
                barTable.getCapacity(),
                barTable.getStatus() // Default constructor value is AVAILABLE
        );
    }
}
