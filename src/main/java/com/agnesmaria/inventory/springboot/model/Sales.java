package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relasi ke Product
    @ManyToOne
    @JoinColumn(name = "product_sku", referencedColumnName = "sku", nullable = false)
    private Product product;

    private int quantity;

    private LocalDateTime timestamp;
}
