package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.ProductCreateRequestDto;
import com.jsalvar.barbilling.dto.response.ProductResponseDto;
import com.jsalvar.barbilling.dto.response.TabResponseDto;
import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
