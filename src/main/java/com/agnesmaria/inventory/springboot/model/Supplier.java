package com.agnesmaria.inventory.springboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}