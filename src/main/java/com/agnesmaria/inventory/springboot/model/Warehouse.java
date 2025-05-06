package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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

    @Column(nullable = false, unique = true, length = 10)
    @Pattern(regexp = "^WH-\\d{3}$", message = "Warehouse code must be in format 'WH-XXX'")
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String location;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}