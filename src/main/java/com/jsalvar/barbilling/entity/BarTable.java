package com.jsalvar.barbilling.entity;

import com.jsalvar.barbilling.entity.enums.TableStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tables")
public class BarTable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String number; // no operations with the table number

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_status")
    private TableStatus status = TableStatus.AVAILABLE; // default value

}
