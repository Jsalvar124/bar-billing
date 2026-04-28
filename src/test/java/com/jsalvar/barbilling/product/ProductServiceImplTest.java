package com.jsalvar.barbilling.product;

import com.jsalvar.barbilling.dto.request.ProductCreateRequestDto;
import com.jsalvar.barbilling.dto.request.ProductUpdateRecordDto;
import com.jsalvar.barbilling.entity.Category;
import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.enums.KitchenType;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.ProductRepository;
import com.jsalvar.barbilling.service.CategoryService;
import com.jsalvar.barbilling.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id("category-id-123")
                .name("Alcohol")
                .kitchenType(KitchenType.BAR)
                .build();

        testProduct = Product.builder()
                .id("product-id-123")
                .name("Margarita")
                .description("Classic margarita cocktail")
                .price(new BigDecimal("12.50"))
                .available(true)
                .active(true)
                .category(testCategory)
                .build();
    }

    @Test
    void findAll_Success() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        List<Product> result = productService.findAll();

        assertEquals(1, result.size());
        assertEquals("Margarita", result.get(0).getName());
    }

    @Test
    void findById_Success() {
        when(productRepository.findById("product-id-123")).thenReturn(Optional.of(testProduct));

        Product result = productService.findById("product-id-123");

        assertNotNull(result);
        assertEquals("product-id-123", result.getId());
    }

    @Test
    void findById_NotFound() {
        when(productRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById("unknown-id"));
    }

    @Test
    void create_Success() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto(
                "Mojito",
                "Refreshing mint cocktail",
                new BigDecimal("10.00"),
                "category-id-123");

        when(categoryService.findById("category-id-123")).thenReturn(testCategory);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId("new-product-id");
            return product;
        });

        Product result = productService.create(dto);

        assertNotNull(result);
        assertEquals("Mojito", result.getName());
        assertEquals(new BigDecimal("10.00"), result.getPrice());
        assertEquals(testCategory, result.getCategory());
    }

    @Test
    void create_CategoryNotFound() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto(
                "Mojito",
                "Refreshing mint cocktail",
                new BigDecimal("10.00"),
                "invalid-category-id");

        when(categoryService.findById("invalid-category-id"))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        assertThrows(ResourceNotFoundException.class, () -> productService.create(dto));
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_Success() {
        ProductUpdateRecordDto dto = new ProductUpdateRecordDto(
                null,
                "Margarita Premium",
                "Premium version with top-shelf tequila",
                new BigDecimal("15.00"),
                null);

        when(productRepository.findById("product-id-123")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.update("product-id-123", dto);

        assertEquals("Margarita Premium", result.getName());
        assertEquals(new BigDecimal("15.00"), result.getPrice());
    }

    @Test
    void update_PartialUpdate_Name() {
        ProductUpdateRecordDto dto = new ProductUpdateRecordDto(
                null,
                "Margarita Updated",
                null,
                null,
                null);

        when(productRepository.findById("product-id-123")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.update("product-id-123", dto);

        assertEquals("Margarita Updated", result.getName());
        assertEquals("Classic margarita cocktail", result.getDescription());
        assertEquals(new BigDecimal("12.50"), result.getPrice());
    }

    @Test
    void update_PartialUpdate_Price() {
        ProductUpdateRecordDto dto = new ProductUpdateRecordDto(
                null,
                null,
                null,
                new BigDecimal("20.00"),
                null);

        when(productRepository.findById("product-id-123")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.update("product-id-123", dto);

        assertEquals(new BigDecimal("20.00"), result.getPrice());
        assertEquals("Margarita", result.getName());
    }

    @Test
    void update_PartialUpdate_Category() {
        Category newCategory = Category.builder()
                .id("new-category-id")
                .name("Premium")
                .kitchenType(KitchenType.BAR)
                .build();

        ProductUpdateRecordDto dto = new ProductUpdateRecordDto(
                null,
                null,
                null,
                null,
                "new-category-id");

        when(productRepository.findById("product-id-123")).thenReturn(Optional.of(testProduct));
        when(categoryService.findById("new-category-id")).thenReturn(newCategory);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.update("product-id-123", dto);

        assertEquals(newCategory, result.getCategory());
    }

    @Test
    void update_NotFound() {
        ProductUpdateRecordDto dto = new ProductUpdateRecordDto(
                null,
                "Updated",
                null,
                null,
                null);

        when(productRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.update("unknown-id", dto));
    }

    @Test
    void delete_Success() {
        when(productRepository.findById("product-id-123")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.delete("product-id-123");

        assertFalse(testProduct.isActive());
        verify(productRepository).save(testProduct);
    }

    @Test
    void delete_NotFound() {
        when(productRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.delete("unknown-id"));
    }

    @Test
    void delete_AlreadyInactive() {
        testProduct.setActive(false);
        when(productRepository.findById("product-id-123")).thenReturn(Optional.of(testProduct));

        assertThrows(UnprocessableEntityException.class, () -> productService.delete("product-id-123"));
        verify(productRepository, never()).save(any());
    }
}