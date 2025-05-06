package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "product_sku", nullable = false)
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
    
    @Column(nullable = false)
    private int quantity;
    
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Constructor manual (alternatif jika tidak pakai @Builder)
    public InventoryItem(Product product, Warehouse warehouse, int quantity) {
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
    }
}