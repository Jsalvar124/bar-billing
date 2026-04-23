package com.jsalvar.barbilling.entity;

import com.jsalvar.barbilling.entity.enums.KitchenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    @Enumerated(EnumType.STRING)
    private KitchenType kitchenType;

    @ManyToMany
    @JoinTable(
            name = "category_tax_rates",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "tax_rate_id")
    )
    private List<TaxRate> taxRates = new ArrayList<>();
}
