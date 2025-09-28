package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "warehouses",
    indexes = {
        @Index(name = "idx_warehouse_code", columnList = "code"),
        @Index(name = "idx_warehouse_name", columnList = "name")
    }
)
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    @Pattern(regexp = "^WH-[A-Z0-9]{3,8}$", message = "Warehouse code must be in format 'WH-XXX' (3-8 alphanumeric chars)")
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String address;  // Changed from location to address

    private Double latitude;
    private Double longitude;

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}