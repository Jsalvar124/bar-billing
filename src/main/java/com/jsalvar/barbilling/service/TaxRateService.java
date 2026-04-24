package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.TaxRateCreateRequestDto;
import com.jsalvar.barbilling.dto.request.TaxRateUpdateRequestDto;
import com.jsalvar.barbilling.entity.TaxRate;

import java.util.List;

public interface TaxRateService {
    List<TaxRate> findAll();
    TaxRate findById(String id);
    TaxRate create(TaxRateCreateRequestDto dto);
    TaxRate update(String id, TaxRateUpdateRequestDto dto);
    void delete(String id);
}