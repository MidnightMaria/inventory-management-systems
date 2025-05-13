package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;  // Import yang diperlukan

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "product_sku")
    private Product product;

    private int quantity;
    private BigDecimal unitPrice;
}
