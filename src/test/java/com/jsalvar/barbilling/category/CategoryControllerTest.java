package com.jsalvar.barbilling.category;

import com.jsalvar.barbilling.controller.CategoryController;
import com.jsalvar.barbilling.dto.request.CategoryCreateRequestDto;
import com.jsalvar.barbilling.dto.request.CategoryUpdateRequestDto;
import com.jsalvar.barbilling.dto.response.CategoryResponseDto;
import com.jsalvar.barbilling.entity.Category;
import com.jsalvar.barbilling.entity.TaxRate;
import com.jsalvar.barbilling.entity.enums.KitchenType;
import com.jsalvar.barbilling.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private Category testCategory;
    private TaxRate testTaxRate;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id("category-id-123")
                .name("Alcohol")
                .kitchenType(KitchenType.BAR)
                .taxRates(new HashSet<>())
                .build();

        testTaxRate = TaxRate.builder()
                .id("tax-rate-id-123")
                .name("IVA")
                .rate(new BigDecimal("0.16"))
                .build();
    }

    private CategoryResponseDto toDto(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getKitchenType(),
                category.getTaxRates().stream()
                        .map(taxRate -> new CategoryResponseDto.TaxRateInfo(
                                taxRate.getId(), taxRate.getName()))
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    @Test
    void findAll_AsAdmin_Returns200() {
        when(categoryService.findAll()).thenReturn(java.util.List.of(testCategory));

        ResponseEntity<java.util.List<CategoryResponseDto>> response = categoryController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void findById_AsAdmin_Returns200() {
        when(categoryService.findById("category-id-123")).thenReturn(testCategory);

        ResponseEntity<CategoryResponseDto> response = categoryController.findById("category-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("category-id-123", response.getBody().id());
    }

    @Test
    void findById_NotFound_ThrowsException() {
        when(categoryService.findById("unknown-id")).thenThrow(new EntityNotFoundException("Not found"));

        assertThrows(EntityNotFoundException.class, () -> categoryController.findById("unknown-id"));
    }

    @Test
    void create_AsAdmin_Returns201() {
        CategoryCreateRequestDto dto = new CategoryCreateRequestDto("Food", KitchenType.KITCHEN);
        when(categoryService.create(any())).thenReturn(testCategory);

        ResponseEntity<CategoryResponseDto> response = categoryController.create(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void update_AsAdmin_Returns200() {
        CategoryUpdateRequestDto dto = new CategoryUpdateRequestDto("Drinks", KitchenType.KITCHEN);
        when(categoryService.update(eq("category-id-123"), any())).thenReturn(testCategory);

        ResponseEntity<CategoryResponseDto> response = categoryController.update("category-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void delete_AsAdmin_Returns204() {
        doNothing().when(categoryService).delete("category-id-123");

        ResponseEntity<Void> response = categoryController.delete("category-id-123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(categoryService).delete("category-id-123");
    }

    @Test
    void addTaxRate_AsAdmin_Returns200() {
        testCategory.getTaxRates().add(testTaxRate);
        when(categoryService.addTaxRate("category-id-123", "tax-rate-id-123")).thenReturn(testCategory);

        ResponseEntity<CategoryResponseDto> response = categoryController.addTaxRate("category-id-123", "tax-rate-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().taxRates().stream()
                .anyMatch(t -> t.id().equals("tax-rate-id-123")));
    }

    @Test
    void removeTaxRate_AsAdmin_Returns200() {
        when(categoryService.removeTaxRate("category-id-123", "tax-rate-id-123")).thenReturn(testCategory);

        ResponseEntity<CategoryResponseDto> response = categoryController.removeTaxRate("category-id-123", "tax-rate-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}