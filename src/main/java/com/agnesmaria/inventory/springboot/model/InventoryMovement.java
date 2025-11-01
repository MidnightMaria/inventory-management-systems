package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = true)
    private Warehouse warehouse; // optional kalau TRANSFER punya dua warehouse

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_warehouse_id", nullable = true)
    private Warehouse fromWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_warehouse_id", nullable = true)
    private Warehouse toWarehouse;

    private Integer quantity;            // ✅ tambahkan ini
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer difference;
    private String movementType;         // e.g. IN, OUT, TRANSFER
    private String reason;
    private String referenceNumber;
    private String performedBy;

    private LocalDateTime createdAt = LocalDateTime.now();
}
