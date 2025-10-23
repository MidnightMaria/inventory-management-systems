package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
    
    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;
    
    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;
    
    @Column(nullable = false)
    private Integer difference;
    
    @Column(name = "movement_type", nullable = false, length = 20)
    private String movementType;
    
    @Column(nullable = false)
    private String reason;
    
    @Column(name = "reference_number", length = 50)
    private String referenceNumber;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Nama user atau sistem yang melakukan perubahan stok.
     * Sekarang disimpan sebagai teks karena sistem sudah tanpa authentication.
     */
    @Column(name = "performed_by", length = 100)
    private String performedBy;
}
