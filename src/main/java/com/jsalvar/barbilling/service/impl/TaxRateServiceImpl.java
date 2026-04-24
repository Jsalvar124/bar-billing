package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.dto.request.TaxRateCreateRequestDto;
import com.jsalvar.barbilling.dto.request.TaxRateUpdateRequestDto;
import com.jsalvar.barbilling.entity.TaxRate;
import com.jsalvar.barbilling.repository.TaxRateRepository;
import com.jsalvar.barbilling.service.TaxRateService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaxRateServiceImpl implements TaxRateService {
    private final TaxRateRepository taxRateRepository;

    public TaxRateServiceImpl(TaxRateRepository taxRateRepository) {
        this.taxRateRepository = taxRateRepository;
    }

    @Override
    public List<TaxRate> findAll() {
        return taxRateRepository.findAll();
    }

    @Override
    public TaxRate findById(String id) {
        return taxRateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tax rate not found"));
    }

    @Override
    @Transactional
    public TaxRate create(TaxRateCreateRequestDto dto) {
        if (taxRateRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Tax rate with this name already exists");
        }

        validateRate(dto.rate());

        TaxRate taxRate = TaxRate.builder()
                .name(dto.name())
                .rate(dto.rate())
                .build();

        return taxRateRepository.save(taxRate);
    }

    @Override
    @Transactional
    public TaxRate update(String id, TaxRateUpdateRequestDto dto) {
        TaxRate taxRate = findById(id);

        if (dto.name() != null && !dto.name().isBlank() && !dto.name().equals(taxRate.getName())) {
            if (taxRateRepository.existsByName(dto.name())) {
                throw new IllegalArgumentException("Tax rate with this name already exists");
            }
            taxRate.setName(dto.name());
        }

        if (dto.rate() != null) {
            validateRate(dto.rate());
            taxRate.setRate(dto.rate());
        }

        return taxRateRepository.save(taxRate);
    }

    @Override
    @Transactional
    public void delete(String id) {
        TaxRate taxRate = findById(id);
        taxRateRepository.delete(taxRate);
    }

    private void validateRate(java.math.BigDecimal rate) {
        if (rate == null) {
            throw new IllegalArgumentException("Rate is required");
        }
        if (rate.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Rate must be greater than 0");
        }
        if (rate.compareTo(java.math.BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Rate must be less than or equal to 1");
        }
    }
}