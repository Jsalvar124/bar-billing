package com.jsalvar.barbilling.entity;

import com.jsalvar.barbilling.entity.enums.PaymentMethod;
import com.jsalvar.barbilling.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "tab_id", nullable = false)
    private Tab tab;

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private UserImpl cashier;

    @Column(nullable = false)
    private BigDecimal total;

    private BigDecimal tip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(nullable = true)
    private LocalDateTime paidAt;
}
