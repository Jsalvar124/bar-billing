package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.CategoryCreateRequestDto;
import com.jsalvar.barbilling.dto.request.CategoryUpdateRequestDto;
import com.jsalvar.barbilling.dto.response.CategoryResponseDto;
import com.jsalvar.barbilling.entity.Category;
import com.jsalvar.barbilling.entity.TaxRate;
import com.jsalvar.barbilling.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Loggable
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponseDto>> findAll() {
        List<CategoryResponseDto> categories = categoryService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(categories);
    }

    @Loggable
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> findById(@PathVariable String id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(toDto(category));
    }

    @Loggable
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> create(@RequestBody CategoryCreateRequestDto dto) {
        Category category = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(category));
    }

    @Loggable
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> update(
            @PathVariable String id,
            @RequestBody CategoryUpdateRequestDto dto) {
        Category category = categoryService.update(id, dto);
        return ResponseEntity.ok(toDto(category));
    }

    @Loggable
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Loggable
    @PatchMapping("/{id}/tax-rates/{taxRateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> addTaxRate(
            @PathVariable String id,
            @PathVariable String taxRateId) {
        Category category = categoryService.addTaxRate(id, taxRateId);
        return ResponseEntity.ok(toDto(category));
    }

    @Loggable
    @DeleteMapping("/{id}/tax-rates/{taxRateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> removeTaxRate(
            @PathVariable String id,
            @PathVariable String taxRateId) {
        Category category = categoryService.removeTaxRate(id, taxRateId);
        return ResponseEntity.ok(toDto(category));
    }

    private CategoryResponseDto toDto(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getKitchenType(),
                category.getTaxRates().stream()
                        .map(taxRate -> new CategoryResponseDto.TaxRateInfo(
                                taxRate.getId(),
                                taxRate.getName()))
                        .collect(java.util.stream.Collectors.toSet())
        );
    }
}