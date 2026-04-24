package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.CategoryCreateRequestDto;
import com.jsalvar.barbilling.dto.request.CategoryUpdateRequestDto;
import com.jsalvar.barbilling.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category findById(String id);
    Category create(CategoryCreateRequestDto dto);
    Category update(String id, CategoryUpdateRequestDto dto);
    void delete(String id);
    
    Category addTaxRate(String categoryId, String taxRateId);
    Category removeTaxRate(String categoryId, String taxRateId);
}