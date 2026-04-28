package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.ProductCreateRequestDto;
import com.jsalvar.barbilling.dto.request.ProductUpdateRecordDto;
import com.jsalvar.barbilling.dto.response.ProductResponseDto;
import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Loggable
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> create(@RequestBody @Valid ProductCreateRequestDto dto) {
        Product product = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDto(product));
    }

    @Loggable
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> findById(@PathVariable String id){
        Product product = productService.findById(id);
        return ResponseEntity.ok().body(toDto(product));
    }

    @Loggable
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> findAll() {
        List<ProductResponseDto> products = productService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(products);
    }

    @Loggable
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable String id,
            @RequestBody ProductUpdateRecordDto dto){
        Product product = productService.update(id, dto);
        return ResponseEntity.ok().body(toDto(product));
    }

    @Loggable
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ProductResponseDto toDto(Product product){
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
}
