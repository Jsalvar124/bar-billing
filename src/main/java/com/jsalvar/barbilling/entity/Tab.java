package com.jsalvar.barbilling.entity;

import com.jsalvar.barbilling.entity.enums.TabStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tabs")
public class Tab {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private BarTable table;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserImpl waiter;

    @Enumerated(EnumType.STRING)
    @Column(name = "tab_status")
    private TabStatus status;

    @CreationTimestamp
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
}
