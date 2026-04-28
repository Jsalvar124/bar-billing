package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.ProductCreateRequestDto;
import com.jsalvar.barbilling.dto.request.ProductUpdateRecordDto;
import com.jsalvar.barbilling.entity.Category;
import com.jsalvar.barbilling.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> findAll();
    Product findById(String id);
    Product create(ProductCreateRequestDto dto);
    Product update(String id, ProductUpdateRecordDto dto);
    void delete(String id);

}
