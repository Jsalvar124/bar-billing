package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.dto.request.CategoryCreateRequestDto;
import com.jsalvar.barbilling.dto.request.CategoryUpdateRequestDto;
import com.jsalvar.barbilling.entity.Category;
import com.jsalvar.barbilling.entity.TaxRate;
import com.jsalvar.barbilling.repository.CategoryRepository;
import com.jsalvar.barbilling.repository.ProductRepository;
import com.jsalvar.barbilling.repository.TaxRateRepository;
import com.jsalvar.barbilling.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final TaxRateRepository taxRateRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, 
                                TaxRateRepository taxRateRepository,
                                ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.taxRateRepository = taxRateRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    @Override
    @Transactional
    public Category create(CategoryCreateRequestDto dto) {
        if (categoryRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        Category category = Category.builder()
                .name(dto.name())
                .kitchenType(dto.kitchenType())
                .build();

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(String id, CategoryUpdateRequestDto dto) {
        Category category = findById(id);

        if (dto.name() != null && !dto.name().isBlank() && !dto.name().equals(category.getName())) {
            if (categoryRepository.existsByName(dto.name())) {
                throw new IllegalArgumentException("Category with this name already exists");
            }
            category.setName(dto.name());
        }

        if (dto.kitchenType() != null) {
            category.setKitchenType(dto.kitchenType());
        }

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Category category = findById(id);

        if (productRepository.countByCategory(category) > 0) {
            throw new IllegalArgumentException("Cannot delete category with associated products");
        }
        category.getTaxRates().clear(); // delete tax rates from category tax-set

        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public Category addTaxRate(String categoryId, String taxRateId) {
        Category category = findById(categoryId);
        TaxRate taxRate = taxRateRepository.findById(taxRateId)
                .orElseThrow(() -> new EntityNotFoundException("Tax rate not found"));

        if (category.getTaxRates().contains(taxRate)) {
            throw new IllegalArgumentException("Tax rate already added to category");
        }

        category.getTaxRates().add(taxRate);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category removeTaxRate(String categoryId, String taxRateId) {
        Category category = findById(categoryId);
        TaxRate taxRate = taxRateRepository.findById(taxRateId)
                .orElseThrow(() -> new EntityNotFoundException("Tax rate not found"));

        if (!category.getTaxRates().contains(taxRate)) {
            throw new IllegalArgumentException("Tax rate not found in category");
        }

        category.getTaxRates().remove(taxRate);
        return categoryRepository.save(category);
    }
}