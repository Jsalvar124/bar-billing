package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.dto.request.ProductCreateRequestDto;
import com.jsalvar.barbilling.dto.request.ProductUpdateRecordDto;
import com.jsalvar.barbilling.entity.Category;
import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.Stock;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.ProductRepository;
import com.jsalvar.barbilling.service.CategoryService;
import com.jsalvar.barbilling.service.ProductService;
import com.jsalvar.barbilling.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final StockService stockService;

    public ProductServiceImpl(ProductRepository productRepository, CategoryService categoryService, StockService stockService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.stockService = stockService;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product with id " +id+" not found"));
    }

    @Override
    @Transactional
    public Product create(ProductCreateRequestDto dto) {

        Category category = categoryService.findById(dto.categoryId()); // Throws error if not found

        Product product = Product.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .category(category)
                .build();

        Stock stock = stockService.initializeStock(product);

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(String id, ProductUpdateRecordDto dto) {
        Product product = findById(id);

        if(dto.name() != null){
            product.setName(dto.name());
        }
        if(dto.description() != null){
            product.setDescription(dto.description());
        }
        if(dto.price() != null){
            product.setPrice(dto.price());
        }
        if (dto.categoryId() != null) {
            Category category = categoryService.findById(dto.categoryId());
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Product product = findById(id);
        if(!product.isActive()){
            throw new UnprocessableEntityException("Product with id " + id + " is already inactive");
        }
        product.setActive(false);
        productRepository.save(product);
    }
}
