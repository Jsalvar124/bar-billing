package com.jsalvar.barbilling.entity;

import com.jsalvar.barbilling.entity.enums.PaymentMethod;
import com.jsalvar.barbilling.entity.enums.BillStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal tax;

    private BigDecimal tip;

    @Column(nullable = false)
    private BigDecimal total;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus billStatus = BillStatus.PENDING;;

    @Column(nullable = true)
    private LocalDateTime paidAt;

    @Column(nullable = true)
    private LocalDateTime cancelledAt;

    @Column(nullable = true)
    private String cancellationReason;
}
