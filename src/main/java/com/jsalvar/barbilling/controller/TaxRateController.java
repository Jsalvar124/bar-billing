package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.TaxRateCreateRequestDto;
import com.jsalvar.barbilling.dto.request.TaxRateUpdateRequestDto;
import com.jsalvar.barbilling.dto.response.TaxRateResponseDto;
import com.jsalvar.barbilling.entity.TaxRate;
import com.jsalvar.barbilling.service.TaxRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tax-rates")
public class TaxRateController {
    private final TaxRateService taxRateService;

    public TaxRateController(TaxRateService taxRateService) {
        this.taxRateService = taxRateService;
    }

    @Loggable
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TaxRateResponseDto>> findAll() {
        List<TaxRateResponseDto> taxRates = taxRateService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(taxRates);
    }

    @Loggable
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaxRateResponseDto> findById(@PathVariable String id) {
        TaxRate taxRate = taxRateService.findById(id);
        return ResponseEntity.ok(toDto(taxRate));
    }

    @Loggable
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaxRateResponseDto> create(@RequestBody TaxRateCreateRequestDto dto) {
        TaxRate taxRate = taxRateService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(taxRate));
    }

    @Loggable
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaxRateResponseDto> update(
            @PathVariable String id,
            @RequestBody TaxRateUpdateRequestDto dto) {
        TaxRate taxRate = taxRateService.update(id, dto);
        return ResponseEntity.ok(toDto(taxRate));
    }

    @Loggable
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        taxRateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private TaxRateResponseDto toDto(TaxRate taxRate) {
        return new TaxRateResponseDto(
                taxRate.getId(),
                taxRate.getName(),
                taxRate.getRate()
        );
    }
}