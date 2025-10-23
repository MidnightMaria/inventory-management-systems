package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "min_stock")
    private int minStock;

    /**
     * 🔹 Jumlah stok total dari semua warehouse.
     * Field ini sekarang menjadi representasi utama dari stok produk.
     */
    @Column(name = "quantity")
    private int quantity;

    /**
     * 🔹 Aktifkan bila sistem dynamic pricing terhubung dengan data science service.
     */
    @Column(name = "dynamic_pricing")
    private boolean dynamicPricing;

    @Column(name = "competitor_price")
    private BigDecimal competitorPrice;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
