package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items;

    @CreationTimestamp
    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate; // dihitung dari lead time supplier

    @Column(name = "total_cost")
    private BigDecimal totalCost; // total biaya pesanan (sum of items + biaya order supplier)

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status;
}