package com.jsalvar.barbilling.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private boolean available;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
