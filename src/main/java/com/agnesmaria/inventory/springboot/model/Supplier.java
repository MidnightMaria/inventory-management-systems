package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "suppliers",
       indexes = {
           @Index(name = "idx_supplier_name", columnList = "name"),
           @Index(name = "idx_supplier_email", columnList = "email")
       })
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
    private String contactPerson;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[0-9\\-\\+]{10,15}$", message = "Phone number should be valid")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    // 🔹 Tambahan untuk optimasi inventory
    @Column(name = "lead_time_days")
    private Integer leadTimeDays; // rata-rata waktu pengiriman (hari)

    @Column(name = "min_order_quantity")
    private Integer minOrderQuantity; // minimum order quantity

    @Column(name = "order_cost")
    private Double orderCost; // biaya per sekali order

    @Column(name = "holding_cost")
    private Double holdingCost; // biaya penyimpanan per unit per periode

    @Column(name = "reliability_score")
    private Double reliabilityScore; // performa supplier (misalnya on-time delivery rate)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relasi opsional ke PurchaseOrder (nanti bisa ditambahkan jika ada model PurchaseOrder)
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<PurchaseOrder> purchaseOrders;
}
