package com.jsalvar.barbilling.product;

import com.jsalvar.barbilling.controller.ProductController;
import com.jsalvar.barbilling.dto.request.ProductCreateRequestDto;
import com.jsalvar.barbilling.dto.request.ProductUpdateRecordDto;
import com.jsalvar.barbilling.dto.response.ProductResponseDto;
import com.jsalvar.barbilling.entity.Category;
import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.enums.KitchenType;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

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

    private ProductResponseDto toDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                new ProductResponseDto.CategroyInfo(
                        product.getCategory().getId(),
                        product.getCategory().getName(),
                        product.getCategory().getKitchenType()
                )
        );
    }

    @Test
    void create_Success_Returns201() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto(
                "Mojito",
                "Refreshing mint cocktail",
                new BigDecimal("10.00"),
                "category-id-123");

        when(productService.create(any())).thenReturn(testProduct);

        ResponseEntity<ProductResponseDto> response = productController.create(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void findAll_Returns200() {
        when(productService.findAll()).thenReturn(List.of(testProduct));

        ResponseEntity<List<ProductResponseDto>> response = productController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void findById_Success_Returns200() {
        when(productService.findById("product-id-123")).thenReturn(testProduct);

        ResponseEntity<ProductResponseDto> response = productController.findById("product-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("product-id-123", response.getBody().id());
    }

    @Test
    void findById_NotFound_ThrowsException() {
        when(productService.findById("unknown-id")).thenThrow(
                new ResourceNotFoundException("Product not found"));

        assertThrows(ResourceNotFoundException.class, () -> productController.findById("unknown-id"));
    }

    @Test
    void update_Success_Returns200() {
        ProductUpdateRecordDto dto = new ProductUpdateRecordDto(
                null,
                "Margarita Premium",
                "Premium version",
                new BigDecimal("15.00"),
                null);

        when(productService.update(eq("product-id-123"), any())).thenReturn(testProduct);

        ResponseEntity<ProductResponseDto> response = productController.update("product-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void update_NotFound_ThrowsException() {
        ProductUpdateRecordDto dto = new ProductUpdateRecordDto(
                null,
                "Updated",
                null,
                null,
                null);

        when(productService.update(eq("unknown-id"), any())).thenThrow(
                new ResourceNotFoundException("Product not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> productController.update("unknown-id", dto));
    }

    @Test
    void delete_Success_Returns204() {
        doNothing().when(productService).delete("product-id-123");

        ResponseEntity<Void> response = productController.delete("product-id-123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productService).delete("product-id-123");
    }

    @Test
    void delete_NotFound_ThrowsException() {
        doThrow(new ResourceNotFoundException("Product not found"))
                .when(productService).delete("unknown-id");

        assertThrows(ResourceNotFoundException.class,
                () -> productController.delete("unknown-id"));
    }
}