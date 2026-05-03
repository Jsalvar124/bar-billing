package com.jsalvar.barbilling.config;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.ProductCreateRequestDto;
import com.jsalvar.barbilling.entity.*;
import com.jsalvar.barbilling.entity.enums.KitchenType;
import com.jsalvar.barbilling.entity.enums.Role;
import com.jsalvar.barbilling.repository.*;
import com.jsalvar.barbilling.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaxRateRepository taxRateRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    private final String adminEmail;

    private final String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, TaxRateRepository taxRateRepository, CategoryRepository categoryRepository, ProductRepository productRepository, ProductService productService, @Value("${app.admin.email}") String adminEmail, @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.taxRateRepository = taxRateRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Loggable
    @Transactional
    @Override
    public void run(String... args) {
        seedUsers();
        seedTaxRates();
        seedCategories();
        seedProducts();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        userRepository.saveAll(List.of(
                buildUser("Admin", "User", adminEmail, adminPassword, Role.ADMIN),
                buildUser("Juan", "Pérez", "waiter@bar.com", "waiter123", Role.WAITER),
                buildUser("Carlos", "García", "cashier@bar.com", "cashier123", Role.CASHIER)
        ));
    }

    private void seedTaxRates() {
        if (taxRateRepository.count() > 0) return;

        taxRateRepository.save(
                TaxRate.builder()
                        .name("Impoconsumo")
                        .rate(new BigDecimal("0.08"))
                        .build()
        );
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;

        TaxRate impoconsumo = taxRateRepository.findByName("Impoconsumo").orElseThrow();

        categoryRepository.saveAll(List.of(
                buildCategory("Beer", KitchenType.BAR, impoconsumo),
                buildCategory("Cocktails", KitchenType.BAR, impoconsumo),
                buildCategory("Spirits", KitchenType.BAR, impoconsumo),
                buildCategory("Wine", KitchenType.BAR, impoconsumo),
                buildCategory("Non-Alcoholic", KitchenType.BAR, impoconsumo),
                buildCategory("Food", KitchenType.KITCHEN, impoconsumo),
                buildCategory("Snacks", KitchenType.KITCHEN, impoconsumo)
        ));
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        Category beer = categoryRepository.findByName("Beer").orElseThrow();
        Category cocktails = categoryRepository.findByName("Cocktails").orElseThrow();
        Category spirits = categoryRepository.findByName("Spirits").orElseThrow();
        Category nonAlcoholic = categoryRepository.findByName("Non-Alcoholic").orElseThrow();
        Category food = categoryRepository.findByName("Food").orElseThrow();
        Category snacks = categoryRepository.findByName("Snacks").orElseThrow();

        List<ProductCreateRequestDto> productDtos = List.of(
                new ProductCreateRequestDto("Pilsen", "Colombian lager", new BigDecimal("5000"), beer.getId()),
                new ProductCreateRequestDto("Aguila", "Colombian beer", new BigDecimal("5000"), beer.getId()),
                new ProductCreateRequestDto("Club Colombia", "Premium Colombian beer", new BigDecimal("7000"), beer.getId()),
                new ProductCreateRequestDto("Poker", "Colombian beer", new BigDecimal("5000"), beer.getId()),
                new ProductCreateRequestDto("Mojito", "Classic rum, mint, lime and soda", new BigDecimal("27000"), cocktails.getId()),
                new ProductCreateRequestDto("Moscow Mule", "Vodka, ginger beer and lime", new BigDecimal("27000"), cocktails.getId()),
                new ProductCreateRequestDto("Paloma Mezcal", "Mezcal, grapefruit juice and soda", new BigDecimal("30000"), cocktails.getId()),
                new ProductCreateRequestDto("Aguardiente Antioqueño", "Colombian anise spirit", new BigDecimal("8000"), spirits.getId()),
                new ProductCreateRequestDto("Ron Medellín", "Colombian rum", new BigDecimal("9000"), spirits.getId()),
                new ProductCreateRequestDto("Agua", "Bottled water", new BigDecimal("3000"), nonAlcoholic.getId()),
                new ProductCreateRequestDto("Jugo Natural", "Fresh fruit juice", new BigDecimal("8000"), nonAlcoholic.getId()),
                new ProductCreateRequestDto("Gaseosa", "Soft drink", new BigDecimal("4000"), nonAlcoholic.getId()),
                new ProductCreateRequestDto("Nachos", "Nachos with cheese dip", new BigDecimal("18000"), food.getId()),
                new ProductCreateRequestDto("Alitas", "Chicken wings", new BigDecimal("25000"), food.getId()),
                new ProductCreateRequestDto("Maní", "Roasted peanuts", new BigDecimal("5000"), snacks.getId()),
                new ProductCreateRequestDto("Papas", "Chips", new BigDecimal("4000"), snacks.getId())
        );

        productDtos.forEach(productService::create);
    }

    private UserImpl buildUser(String name, String lastname, String email, String password, Role role) {
        return UserImpl.builder()
                .name(name)
                .lastname(lastname)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .active(true)
                .build();
    }

    private Category buildCategory(String name, KitchenType kitchenType, TaxRate taxRate) {
        return Category.builder()
                .name(name)
                .kitchenType(kitchenType)
                .taxRates(Set.of(taxRate))
                .build();
    }

    private Product buildProduct(String name, String description, BigDecimal price, Category category) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .build();
    }
}
