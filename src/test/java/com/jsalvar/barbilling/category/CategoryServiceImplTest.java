package com.jsalvar.barbilling.category;

import com.jsalvar.barbilling.dto.request.CategoryCreateRequestDto;
import com.jsalvar.barbilling.dto.request.CategoryUpdateRequestDto;
import com.jsalvar.barbilling.entity.Category;
import com.jsalvar.barbilling.entity.TaxRate;
import com.jsalvar.barbilling.entity.enums.KitchenType;
import com.jsalvar.barbilling.repository.CategoryRepository;
import com.jsalvar.barbilling.repository.ProductRepository;
import com.jsalvar.barbilling.repository.TaxRateRepository;
import com.jsalvar.barbilling.service.impl.CategoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TaxRateRepository taxRateRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

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

    @Test
    void findAll_Success() {
        when(categoryRepository.findAll()).thenReturn(List.of(testCategory));

        List<Category> result = categoryService.findAll();

        assertEquals(1, result.size());
        assertEquals("Alcohol", result.get(0).getName());
    }

    @Test
    void findById_Success() {
        when(categoryRepository.findById("category-id-123")).thenReturn(Optional.of(testCategory));

        Category result = categoryService.findById("category-id-123");

        assertNotNull(result);
        assertEquals("category-id-123", result.getId());
    }

    @Test
    void findById_NotFound() {
        when(categoryRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.findById("unknown-id"));
    }

    @Test
    void create_Success() {
        CategoryCreateRequestDto dto = new CategoryCreateRequestDto("Food", KitchenType.KITCHEN);
        when(categoryRepository.existsByName("Food")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId("new-category-id");
            return category;
        });

        Category result = categoryService.create(dto);

        assertNotNull(result);
        assertEquals("Food", result.getName());
        assertEquals(KitchenType.KITCHEN, result.getKitchenType());
    }

    @Test
    void create_DuplicateName() {
        CategoryCreateRequestDto dto = new CategoryCreateRequestDto("Alcohol", KitchenType.BAR);
        when(categoryRepository.existsByName("Alcohol")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.create(dto));
    }

    @Test
    void update_Success() {
        CategoryUpdateRequestDto dto = new CategoryUpdateRequestDto("Drinks", KitchenType.KITCHEN);
        when(categoryRepository.findById("category-id-123")).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Drinks")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.update("category-id-123", dto);

        assertEquals("Drinks", result.getName());
        assertEquals(KitchenType.KITCHEN, result.getKitchenType());
    }

    @Test
    void update_NotFound() {
        CategoryUpdateRequestDto dto = new CategoryUpdateRequestDto("Drinks", KitchenType.KITCHEN);
        when(categoryRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.update("unknown-id", dto));
    }

    @Test
    void update_PartialUpdate() {
        CategoryUpdateRequestDto dto = new CategoryUpdateRequestDto(null, KitchenType.KITCHEN);
        when(categoryRepository.findById("category-id-123")).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.update("category-id-123", dto);

        assertEquals("Alcohol", result.getName());
        assertEquals(KitchenType.KITCHEN, result.getKitchenType());
    }

    @Test
    void delete_Success() {
        when(categoryRepository.findById("category-id-123")).thenReturn(Optional.of(testCategory));
        when(productRepository.countByCategory(testCategory)).thenReturn(0L);
        doNothing().when(categoryRepository).delete(testCategory);

        categoryService.delete("category-id-123");

        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void delete_WithProducts_ThrowsException() {
        when(categoryRepository.findById("category-id-123")).thenReturn(Optional.of(testCategory));
        when(productRepository.countByCategory(testCategory)).thenReturn(5L);

        assertThrows(IllegalArgumentException.class, () -> categoryService.delete("category-id-123"));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void addTaxRate_Success() {
        when(categoryRepository.findById("category-id-123")).thenReturn(Optional.of(testCategory));
        when(taxRateRepository.findById("tax-rate-id-123")).thenReturn(Optional.of(testTaxRate));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.addTaxRate("category-id-123", "tax-rate-id-123");

        assertTrue(result.getTaxRates().contains(testTaxRate));
    }

    @Test
    void addTaxRate_AlreadyExists() {
        testCategory.getTaxRates().add(testTaxRate);
        when(categoryRepository.findById("category-id-123")).thenReturn(Optional.of(testCategory));
        when(taxRateRepository.findById("tax-rate-id-123")).thenReturn(Optional.of(testTaxRate));

        assertThrows(IllegalArgumentException.class, 
                () -> categoryService.addTaxRate("category-id-123", "tax-rate-id-123"));
    }

    @Test
    void removeTaxRate_Success() {
        testCategory.getTaxRates().add(testTaxRate);
        when(categoryRepository.findById("category-id-123")).thenReturn(Optional.of(testCategory));
        when(taxRateRepository.findById("tax-rate-id-123")).thenReturn(Optional.of(testTaxRate));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.removeTaxRate("category-id-123", "tax-rate-id-123");

        assertFalse(result.getTaxRates().contains(testTaxRate));
    }

    @Test
    void removeTaxRate_NotFound() {
        when(categoryRepository.findById("category-id-123")).thenReturn(Optional.of(testCategory));
        when(taxRateRepository.findById("tax-rate-id-123")).thenReturn(Optional.of(testTaxRate));

        assertThrows(IllegalArgumentException.class, 
                () -> categoryService.removeTaxRate("category-id-123", "tax-rate-id-123"));
    }
}